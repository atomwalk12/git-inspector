package gitinsp.application
import akka.actor.ActorSystem
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.marshalling.sse.EventStreamMarshalling.*
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import gitinsp.domain.ChatService
import gitinsp.domain.IngestorService
import gitinsp.domain.Pipeline
import gitinsp.infrastructure.CacheService
import gitinsp.infrastructure.ContentService
import gitinsp.infrastructure.GithubWrapperService
import gitinsp.infrastructure.factories.RAGComponentFactoryImpl
import gitinsp.infrastructure.strategies.IngestionStrategyFactory
import io.circe.*
import io.circe.parser.*

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt

object GitInspector extends LazyLogging:

  // Config
  val config                                      = ConfigFactory.load()
  implicit val system: ActorSystem                = ActorSystem("langchain-coordinator", config)
  implicit val materializer: Materializer         = Materializer(system)
  implicit val executionContext: ExecutionContext = system.dispatcher

  // Traditional Java-style main method that SBT can find
  def main(args: Array[String]): Unit =
    scalaFrontend()

  @main def pythonFrontend(): Unit =
    API(prettyFmt = true)

  @main def scalaFrontend(): Unit =
    API(prettyFmt = false)

  def API(prettyFmt: Boolean): Unit =
    // Services
    val factory         = RAGComponentFactoryImpl(config)
    val cacheService    = CacheService(factory)
    val ingestorService = IngestorService(cacheService, config, IngestionStrategyFactory)
    val gitService      = GithubWrapperService()
    val chatService     = ChatService(prettyFmt, ContentService)
    val pipeline        = Pipeline(chatService, cacheService, ingestorService, gitService)

    // Config
    val langchainCoordinator = LangchainCoordinator(pipeline, gitService, prettyFmt)

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
