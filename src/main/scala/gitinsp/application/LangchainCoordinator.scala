package gitinsp.application

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.sse.ServerSentEvent
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import com.typesafe.scalalogging.LazyLogging
import gitinsp.domain.interfaces.application.Pipeline
import gitinsp.domain.interfaces.infrastructure.GithubWrapperService
import gitinsp.domain.models.Category
import gitinsp.domain.models.RepositoryWithLanguages
import gitinsp.domain.models.URL
import io.circe.*
import io.circe.syntax.*

import scala.concurrent.ExecutionContext
import scala.util.Failure
import scala.util.Success
import scala.util.Try
trait LangchainCoordinator:
  // Aliases
  type StreamingResponse = Source[ServerSentEvent, NotUsed]

  // Methods
  def listIndexes(): Try[String]
  def chat(msg: String, indexNameOpt: Option[String]): Try[StreamingResponse]
  def generateIndex(repoUrl: String, languages: String): Try[String]
  def fetchRepository(link: String, format: String, extension: String): Try[String]
  def deleteIndex(indexName: String): Try[String]
  def start(routes: Route): Unit

object LangchainCoordinator:
  def apply(pipeline: Pipeline, gitWrapper: GithubWrapperService, prettyFmt: Boolean)(using
    system: ActorSystem,
    materializer: Materializer,
    executionContext: ExecutionContext,
  ): LangchainCoordinator = new LangchainCoordinatorImpl(pipeline, gitWrapper, prettyFmt)

  private class LangchainCoordinatorImpl(using
    system: ActorSystem,
    materializer: Materializer,
    executionContext: ExecutionContext,
  )(pipeline: Pipeline, gitService: GithubWrapperService, prettyFmt: Boolean)
      extends LangchainCoordinator
      with LazyLogging:

    override def listIndexes(): Try[String] =
      pipeline.listIndexes().map(
        indexes => {
          // Map the results back into a list of strings representing the links
          val indexNames: List[String] = indexes.map(index => index.toURL().value)
          logger.info(s"Indexes: $indexNames")

          // Return the indexes in a JSON format
          val jsonResponse = Map("indexes" -> (List("") ++ indexNames)).asJson.noSpaces
          jsonResponse
        },
      ).recover {
        case ex: Exception =>
          logger.error("Error listing indexes", ex)
          Map("error" -> "Error listing indexes").asJson.noSpaces
      }

    override def chat(msg: String, indexNameOpt: Option[String]): Try[StreamingResponse] =
      // Convert option to URL if provided
      val indexURL = indexNameOpt
        .filter(_.nonEmpty)
        .map(name => URL(name).toAIServiceURL())

      // Use the AI service to process the chat request
      pipeline
        .getAIService(indexURL)
        .map(
          service =>
            pipeline.chat(msg, service).map(
              chunk =>
                ServerSentEvent(Map("text" -> chunk).asJson.noSpaces),
            ),
        )
        .recoverWith {
          case ex: IllegalStateException =>
            val indexName = indexNameOpt.getOrElse("")
            Success(Source.single(ServerSentEvent(s"Error: Couldn't find index: $indexName")))
          case ex: Exception =>
            Success(Source.single(ServerSentEvent(s"Error: ${ex.getMessage}")))
        }

    override def fetchRepository(link: String, format: String, extension: String): Try[String] =
      // Detect the languages in the repository
      val languages = RepositoryWithLanguages.detectLanguage(extension).getOrElse(List())

      // Fetch the repository and return the content
      gitService
        .fetchRepository(URL(link), languages)
        .map(content => if content.trim.isEmpty then "No content found" else content)
        .recoverWith {
          case exception =>
            Failure(new Exception(s"Failed to fetch repository: ${exception.getMessage}"))
        }

    override def generateIndex(repoUrl: String, languages: String): Try[String] =
      // Parse the data
      val repoURL       = URL(repoUrl)
      val languagesList = RepositoryWithLanguages.detectLanguage(languages).getOrElse(List())

      // Build and index the repository in a more functional way
      gitService
        .buildRepository(repoURL, languagesList)
        .flatMap(repo => pipeline.regenerateIndex(repo))
        .map(
          _ =>
            Map(
              "result"    -> "Index generated successfully",
              "indexName" -> repoURL.value,
            ).asJson.noSpaces,
        )

    override def deleteIndex(indexName: String): Try[String] =
      val url = URL(indexName)

      def deleteWithLogging(category: Category): Try[String] =
        pipeline.deleteIndex(url, category)
          .map(
            _ => {
              logger.info(s"${category.toString} index deleted successfully: $indexName")
              s"${category.toString} index deleted successfully"
            },
          )
          .recoverWith {
            case ex =>
              logger.error(s"Error deleting ${category.toString} index: $indexName", ex)
              Failure(ex)
          }

      for {
        textResult <- deleteWithLogging(Category.TEXT)
        codeResult <- deleteWithLogging(Category.CODE)
      } yield Map("result" -> s"$textResult, $codeResult").asJson.noSpaces

    override def start(routes: Route): Unit =
      Http().newServerAt("localhost", 8080).bind(routes)
