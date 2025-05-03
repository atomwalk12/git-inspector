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
import gitinsp.domain.models.Category
import gitinsp.domain.models.GitRepository
import gitinsp.domain.models.URL
import io.circe.*
import io.circe.syntax.*

import scala.concurrent.ExecutionContext
import scala.util.Failure
import scala.util.Success
import scala.util.Try

/** Main coordinator for the Langchain application
  * Handles all interactions with the AI services and repositories
  */
trait LangchainCoordinator:
  // Aliases
  type StreamingResponse = Source[ServerSentEvent, NotUsed]

  // Methods
  /** Retrieves a list of all available indexes in the system
    * @return A JSON string containing the list of index names wrapped in a Try
    */
  def listIndexes(): Try[String]

  /** Performs a chat interaction with an AI service
    * @param msg The user's message to send to the AI
    * @param indexNameOpt Optional name of the index to use for RAG context
    * @return A streaming response containing AI-generated content wrapped in a Try
    */
  def chat(msg: String, indexNameOpt: Option[String]): Try[StreamingResponse]

  /** Generates a new index for a repository
    * @param repoUrl URL of the repository to index
    * @param languages Comma-separated list of file extensions to include
    * @return A JSON string with the result and index name wrapped in a Try
    */
  def generateIndex(repoUrl: String, languages: String): Try[String]

  /** Fetches repository content
    * @param link URL of the repository to fetch
    * @param format Format of the content to retrieve
    * @param extension File extensions to filter for
    * @return Repository content as a string wrapped in a Try
    */
  def fetchRepository(link: String, format: String, extension: String): Try[String]

  /** Deletes an index from the system
    * @param indexName Name of the index to delete
    * @return A JSON string containing the result of the deletion wrapped in a Try
    */
  def deleteIndex(indexName: String): Try[String]

  /** Starts the HTTP server with the specified routes
    * @param routes The configured API routes
    */
  def start(routes: Route): Unit

object LangchainCoordinator:
  def apply(pipeline: Pipeline, prettyFmt: Boolean)(using
    system: ActorSystem,
    materializer: Materializer,
    executionContext: ExecutionContext,
  ): LangchainCoordinator = new LangchainCoordinatorImpl(pipeline, prettyFmt)

  private class LangchainCoordinatorImpl(using
    system: ActorSystem,
    materializer: Materializer,
    executionContext: ExecutionContext,
  )(pipeline: Pipeline, prettyFmt: Boolean)
      extends LangchainCoordinator
      with LazyLogging:

    override def listIndexes(): Try[String] =
      pipeline.listIndexes().map(
        indexes => {
          // Map the results back into a list of strings representing the links
          val indexNames: List[String] = indexes.map(index => index.toURL().value)
          logger.info(s"Indexes: $indexNames")

          // Return the indexes in a JSON format
          val jsonResponse = Map("indexes" -> indexNames).asJson.noSpaces
          jsonResponse
        },
      ).recoverWith {
        case ex: Exception =>
          logger.error("Error listing indexes", ex.getMessage)
          Failure(ex)
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
      val languages = GitRepository.detectLanguage(extension).getOrElse(List())

      // Fetch the repository and return the content
      pipeline
        .fetchRepository(URL(link), languages)
        .map(content => if content.trim.isEmpty then "No content found" else content)
        .recoverWith {
          case exception =>
            Failure(new Exception(s"Failed to fetch repository: ${exception.getMessage}"))
        }

    override def generateIndex(repoUrl: String, languages: String): Try[String] =
      // Parse the data
      val repoURL       = URL(repoUrl)
      val languagesList = GitRepository.detectLanguage(languages).getOrElse(List())

      if languagesList.isEmpty then
        Failure(new IllegalArgumentException(s"No languages detected for $repoUrl"))
      else
        // Build and index the repository in a more functional way
        pipeline
          .buildRepository(repoURL, languagesList)
          .flatMap(repo => pipeline.regenerateIndex(repo))
          .map(
            _ =>
              Map(
                "result" -> "Index generated successfully",
                // Strip out the http://
                "indexName" -> repoURL.toAIServiceURL().toURL().value,
              ).asJson.noSpaces,
          )

    override def deleteIndex(indexName: String): Try[String] =
      val url = URL(indexName)

      def deleteWithLogging(category: Category): Try[String] =
        pipeline.deleteIndex(url, category)
          .map(
            _ => {
              logger.info(s"${category.toString} index deleted successfully: $indexName")
              s"$indexName deleted successfully"
            },
          )
          .recover {
            case ex =>
              logger.warn(s"Error deleting ${category.toString} index: $indexName")
              s"$indexName not found"
          }

      for {
        textResult <- deleteWithLogging(Category.TEXT)
        codeResult <- deleteWithLogging(Category.CODE)
      } yield Map("result" -> s"$textResult, $codeResult").asJson.noSpaces

    override def start(routes: Route): Unit =
      Http().newServerAt("localhost", 8080).bind(routes)
