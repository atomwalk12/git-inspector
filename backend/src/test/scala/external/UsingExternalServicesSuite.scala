package gitinsp.tests.external

import akka.actor.ActorSystem
import akka.stream.Materializer
import com.typesafe.config.ConfigFactory
import gitinsp.domain.ChatService
import gitinsp.domain.PipelineService
import gitinsp.domain.interfaces.application.IngestorService
import gitinsp.domain.interfaces.infrastructure.GithubWrapperService
import gitinsp.domain.interfaces.infrastructure.RAGComponentFactory
import gitinsp.domain.models.CodeFile
import gitinsp.domain.models.GitRepository
import gitinsp.domain.models.Language
import gitinsp.domain.models.URL
import gitinsp.infrastructure.CacheService
import gitinsp.infrastructure.ContentService
import gitinsp.infrastructure.FetchingService
import gitinsp.infrastructure.GithubWrapperService
import gitinsp.tests.externalServiceTag
import gitinsp.tests.repoName
import org.mockito.Mockito.spy
import org.scalatest.BeforeAndAfterAll
import org.scalatest.BeforeAndAfterEach
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar

import scala.concurrent.ExecutionContext
import scala.util.Failure
import scala.util.Success

class UsingExternalServicesSuite extends AnyFlatSpec with Matchers with MockitoSugar
    with BeforeAndAfterAll
    with BeforeAndAfterEach:

  // Setup dependencies
  val config                                      = ConfigFactory.load()
  implicit val system: ActorSystem                = ActorSystem("pipeline-test-system")
  implicit val materializer: Materializer         = Materializer(system)
  implicit val executionContext: ExecutionContext = system.dispatcher

  // Data
  val url = URL(repoName)

  // Setup mocks
  val mockRAGFactory      = mock[RAGComponentFactory]
  val mockIngestorService = mock[IngestorService]
  val mockChatService     = spy(ChatService(false, ContentService))
  val mockCacheService    = spy(CacheService(mockRAGFactory))
  val githubService       = mock[GithubWrapperService]

  it should "be able to process documents" taggedAs externalServiceTag in:
    // Setup classes
    val pipe =
      PipelineService(mockChatService, mockCacheService, mockIngestorService, githubService)

    // Setup data
    val languages = GitRepository.detectLanguages("scala,md,py")
    val doc1      = CodeFile("def test()", Language.SCALA, "test.scala", 1000, 100)
    val doc2      = CodeFile("# Hello, world!", Language.MARKDOWN, "test.md", 1000, 100)
    val doc3      = CodeFile("print('Hello, world!')", Language.PYTHON, "test.py", 1000, 100)
    val docs      = List(doc1, doc1, doc2, doc3)
    val repo      = GitRepository(url, languages, docs)

    // Execute
    val source = pipe.regenerateIndex(repo)

  "GithubWrapperService" should "successfully fetch a repository" taggedAs externalServiceTag in:
    // Create the service with mock config
    val service = GithubWrapperService(config, FetchingService())

    // Use a known public repo for testing
    val languages = GitRepository.detectLanguage("scala,md").getOrElse(List())

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

  it should "successfully fetch plain text" taggedAs externalServiceTag in:
    // Create the service with mock config
    val service = GithubWrapperService(config, FetchingService())

    // Use a known public repo for testing
    val languages = GitRepository.detectLanguage("scala,md").getOrElse(List())

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
