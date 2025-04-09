package gitinsp.application

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.marshalling.sse.EventStreamMarshalling.*
import akka.http.scaladsl.model.sse.ServerSentEvent
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import gitinsp.domain.ChatService
import gitinsp.domain.GithubWrapperService
import gitinsp.domain.IngestorService
import gitinsp.domain.Pipeline as PipelineService
import gitinsp.infrastructure.CacheService
import gitinsp.utils.Category
import gitinsp.utils.RepositoryWithLanguages
import gitinsp.utils.URL
import io.circe.*
import io.circe.parser.*
import io.circe.syntax.*

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt
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
  def apply(prettyFmt: Boolean): LangchainCoordinator = new LangchainCoordinatorImpl(prettyFmt)

  private class LangchainCoordinatorImpl(prettyFmt: Boolean) extends LangchainCoordinator
      with LazyLogging:

    // Config
    val config                                      = ConfigFactory.load()
    implicit val system: ActorSystem                = ActorSystem("langchain-coordinator", config)
    implicit val materializer: Materializer         = Materializer(system)
    implicit val executionContext: ExecutionContext = system.dispatcher

    // Services
    val cacheService    = CacheService()
    val ingestorService = IngestorService()
    val gitService      = GithubWrapperService()
    val chatService     = ChatService(prettyFmt = prettyFmt)
    val pipeline        = PipelineService(chatService, cacheService, ingestorService, gitService)

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

object GitInspector extends LazyLogging:

  @main def pythonFrontend(): Unit =
    API(prettyFmt = true)

  @main def scalaFrontend(): Unit =
    API(prettyFmt = false)

  def API(prettyFmt: Boolean): Unit =
    // Config
    val langchainCoordinator = LangchainCoordinator(prettyFmt)
    val config               = ConfigFactory.load()

    // Used for listing current indexes
    val listIndexes: Route = path("list_indexes"):
      get:
        complete:
          langchainCoordinator.listIndexes()

    // Used for chatting with the AI
    val chat: Route = path("chat"):
      get:
        parameters("msg", "indexName".?): (msg, indexNameOpt) =>
          complete:
            langchainCoordinator.chat(msg, indexNameOpt)

    // Used for generating an index
    val generateIndex: Route = path("generate"):
      withRequestTimeout(config.getInt("gitinsp.request.timeout").seconds):
        post:
          formFields("data"): (data) =>
            decode[Map[String, String]](data) match
              case Right(json) =>
                val repoUrl   = json("indexName")
                val languages = json("extensions")
                complete:
                  langchainCoordinator.generateIndex(repoUrl, languages)
              case Left(error) =>
                complete(s"Error parsing data: ${error.getMessage}")

    // Used for fetching a repository
    val fetchRepository: Route = path("fetch"):
      get:
        parameters("link", "format", "extension"): (link, format, extension) =>
          complete {
            langchainCoordinator.fetchRepository(link, format, extension)
          }

    // Setup the routes
    val combinedRoutes = listIndexes ~ chat ~ generateIndex ~ fetchRepository

    // Start the HTTP server
    langchainCoordinator.start(combinedRoutes)
