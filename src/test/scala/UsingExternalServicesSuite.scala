package gitinsp.tests.external
import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Source
import com.typesafe.config.Config
import gitinsp.chatpipeline.RAGComponentFactory
import gitinsp.domain.ChatService
import gitinsp.domain.GithubWrapperService
import gitinsp.domain.IngestorService
import gitinsp.domain.Pipeline
import gitinsp.infrastructure.CacheService
import gitinsp.infrastructure.ContentFormatter
import gitinsp.infrastructure.URLClient
import gitinsp.tests.ExternalService
import gitinsp.utils.Assistant
import gitinsp.utils.CodeFile
import gitinsp.utils.Language
import gitinsp.utils.RepositoryWithLanguages
import gitinsp.utils.URL
import org.mockito.Mockito.spy
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterAll
import org.scalatest.BeforeAndAfterEach
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar

import scala.concurrent.Await
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt
import scala.util.Failure
import scala.util.Success

class PipelineTest extends AnyFlatSpec with Matchers with MockitoSugar with BeforeAndAfterAll
    with BeforeAndAfterEach:

  // Setup dependencies
  val config                                      = mock[Config]
  implicit val system: ActorSystem                = ActorSystem("pipeline-test-system")
  implicit val materializer: Materializer         = Materializer(system)
  implicit val executionContext: ExecutionContext = system.dispatcher

  // Data
  val url = URL("https://github.com/atomwalk12/PPS-22-git-insp")

  // Setup mocks
  val mockRAGFactory      = mock[RAGComponentFactory]
  val mockIngestorService = mock[IngestorService]
  val mockChatService     = spy(ChatService(false, ContentFormatter))
  val mockCacheService    = spy(CacheService(mockRAGFactory))

  override def beforeAll(): Unit =
    // Setup config
    when(config.getString("gitinsp.ollama.url")).thenReturn("http://localhost:11434")
    when(config.getString("gitinsp.code-embedding.model")).thenReturn("nomic-embed-text")
    when(config.getString("gitinsp.text-embedding.model")).thenReturn("nomic-embed-text")
    when(config.getString("gitinsp.models.default-model")).thenReturn("llama3.3")
    when(config.getString("gitinsp.rag.model")).thenReturn("llama3.3")
    when(config.getInt("gitinsp.code-embedding.chunk-size")).thenReturn(1000)
    when(config.getInt("gitinsp.text-embedding.chunk-size")).thenReturn(1000)
    when(config.getInt("gitinsp.code-embedding.chunk-overlap")).thenReturn(200)
    when(config.getInt("gitinsp.text-embedding.chunk-overlap")).thenReturn(200)
    when(config.getInt("gitinsp.timeout")).thenReturn(5000)

  "Pipeline with external services" should "be able to execute" taggedAs ExternalService in:
    // Setup
    val pipe = Pipeline(using system, materializer, executionContext)

    // Execute
    val index     = url.toAIServiceURL()
    val aiService = pipe.getAIService(Some(index))

    // Verify
    aiService.fold(
      (ex: Throwable) => fail(s"Failed to get AI service: ${ex.getMessage}"),
      (assistant: Assistant) => {
        // Execute
        val source = pipe.chat("Hi!", assistant)

        // Verify
        source.fold(
          (ex: Throwable) => fail(s"Chat failed: ${ex.getMessage}"),
          (chatSource: Source[String, NotUsed]) => {
            val future  = chatSource.map(println).runWith(Sink.seq)
            val results = Await.result(future, 2.minutes)
          },
        )
      },
    )

  it should "be able to execute with content retrieval" taggedAs ExternalService in:
    // Setup
    val pipe  = Pipeline(using system, materializer, executionContext)
    val index = url.toAIServiceURL()

    // Execute
    val aiService = pipe.getAIService(Some(index))

    // Verify
    aiService.fold(
      (ex: Throwable) => fail(s"Failed to get AI service: ${ex.getMessage}"),
      (assistant: Assistant) => {
        // Execute
        val source = pipe.chat("Hi!", assistant)

        // Verify
        source.fold(
          (ex: Throwable) => fail(s"Chat failed: ${ex.getMessage}"),
          (chatSource: Source[String, NotUsed]) => {
            val future  = chatSource.map(println).runWith(Sink.seq)
            val results = Await.result(future, 2.minutes)
          },
        )
      },
    )

  it should "be able to process documents" taggedAs ExternalService in:
    // Setup classes
    val pipeWithExternalServices = Pipeline(using system, materializer, executionContext)

    // Setup data
    val languages = RepositoryWithLanguages.detectLanguages("scala,md,py")
    val doc1      = CodeFile("def test()", Language.SCALA, "test.scala")
    val doc2      = CodeFile("# Hello, world!", Language.MARKDOWN, "test.md")
    val doc3      = CodeFile("print('Hello, world!')", Language.PYTHON, "test.py")
    val docs      = List(doc1, doc1, doc2, doc3)
    val repo      = RepositoryWithLanguages(url, languages, docs)

    // Execute
    val source = pipeWithExternalServices.regenerateIndex(repo)

  "GithubWrapperService" should "successfully fetch a repository" taggedAs ExternalService in:
    // Create the service with mock config
    val service = GithubWrapperService(config, URLClient())

    // Use a known public repo for testing
    val languages = RepositoryWithLanguages.detectLanguage("scala,md").getOrElse(List())

    // Execute
    val result = service.buildRepository(url, languages)

    // Verify
    result.isSuccess shouldBe true
    result match
      case Success(repo) =>
        repo.url shouldBe url
        repo.languages should contain(Language.SCALA)
        repo.languages should contain(Language.MARKDOWN)
        repo.docs should not be empty
      case Failure(ex) =>
        fail(s"Repository fetch failed: ${ex.getMessage}")

  it should "successfully fetch plain text" taggedAs ExternalService in:
    // Create the service with mock config
    val service = GithubWrapperService(config, URLClient())

    // Use a known public repo for testing
    val languages = RepositoryWithLanguages.detectLanguage("scala,md").getOrElse(List())

    // Execute
    val result = service.fetchRepository(url, languages)

    // Verify
    result.isSuccess shouldBe true
    result match
      case Success(plainText) =>
        println(plainText)
        plainText should not be empty
      case Failure(ex) =>
        fail(s"Repository fetch failed: ${ex.getMessage}")
