package gitinsp.tests.requirements

import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.Sink
import com.typesafe.config.ConfigFactory
import gitinsp.domain.ChatService
import gitinsp.domain.IngestorService
import gitinsp.domain.PipelineService
import gitinsp.domain.models.GitRepository
import gitinsp.domain.models.Language
import gitinsp.domain.models.URL
import gitinsp.infrastructure.CacheService
import gitinsp.infrastructure.ContentService
import gitinsp.infrastructure.FetchingService
import gitinsp.infrastructure.GithubWrapperService
import gitinsp.infrastructure.factories.RAGComponentFactoryImpl
import gitinsp.infrastructure.strategies.IngestionStrategyFactory
import gitinsp.tests.externalServiceTag
import gitinsp.tests.repoName
import org.scalatest.GivenWhenThen
import org.scalatest.featurespec.AnyFeatureSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar

import scala.concurrent.Await
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.*
import scala.util.Try

class SystemFunctionalRequirementsSuite extends AnyFeatureSpec with GivenWhenThen with MockitoSugar
    with Matchers:

  // Setup test environment
  implicit val system: ActorSystem                = ActorSystem("business-requirements-test-system")
  implicit val materializer: Materializer         = Materializer(system)
  implicit val executionContext: ExecutionContext = system.dispatcher
  val config                                      = ConfigFactory.load()

  Feature("FR2.1: Repository Cloning and Management") {
    Scenario("Clone a valid repository within acceptable time", externalServiceTag) {
      Given("A valid repository URL and language configuration")
      val githubService   = GithubWrapperService(config, FetchingService())
      val ragFactory      = RAGComponentFactoryImpl(config)
      val cacheService    = CacheService(ragFactory)
      val ingestorService = IngestorService(cacheService, config, IngestionStrategyFactory)

      And("A set of specific languages to filter")
      val validUrl  = URL(repoName)
      val languages = GitRepository.detectLanguages("scala,md")

      When("The repository is cloned")
      val startTime      = System.currentTimeMillis()
      val result         = githubService.buildRepository(validUrl, languages)
      val processingTime = System.currentTimeMillis() - startTime

      Then("The cloning operation should succeed")
      result.isSuccess shouldBe true

      And("The cloning operation should complete within the specified time")
      processingTime should be <= 30000L // 30 seconds max

      And("The repository should contain the expected languages")
      result.foreach {
        repo =>
          repo.languages should contain(Language.SCALA)
          repo.languages should contain(Language.MARKDOWN)
      }
    }

    Scenario("Detect invalid repository URLs", externalServiceTag) {
      Given("An invalid repository URL")
      val githubService = GithubWrapperService(config, FetchingService())
      val invalidUrl    = URL("https://github.com/nonexistent/repository")
      val languages     = GitRepository.detectLanguages("scala")

      When("Attempting to clone the repository")
      val result = githubService.buildRepository(invalidUrl, languages)

      Then("The operation should fail with appropriate error")
      result.isFailure shouldBe true
    }
  }

  Feature("FR2.2: Vector Database Generation for RAG"):
    Scenario("Cache repository code embeddings in Qdrant", externalServiceTag):
      Given("A configured pipeline and repository")
      val ragFactory      = RAGComponentFactoryImpl(config)
      val cacheService    = CacheService(ragFactory)
      val githubService   = GithubWrapperService(config, FetchingService())
      val chatService     = ChatService(false, ContentService)
      val ingestorService = IngestorService(cacheService, config, IngestionStrategyFactory)
      val pipeline = PipelineService(chatService, cacheService, ingestorService, githubService)

      val validUrl  = URL("https://github.com/atomwalk12/deep-bridge-survey")
      val languages = GitRepository.detectLanguages("py,md")

      And("A repository has been successfully built")
      val repository = githubService.buildRepository(
        validUrl,
        languages,
      ).getOrElse(fail("Failed to build repository"))

      When("The repository code is ingested and embeddings are generated")
      val ingestResult = Try(ingestorService.ingest(repository))

      Then("The ingestion should succeed")
      ingestResult.isSuccess shouldBe true

      And("The cache should contain embeddings for the repository")
      val hasEmbeddings =
        pipeline.listIndexes().getOrElse(List.empty).contains(validUrl.toAIServiceURL())
      hasEmbeddings shouldBe true

  Feature("FR2.3: Vector Database Implementation"):
    Scenario("Vector database implementation"):
      info("This has already been shown in the FR1.2 and FR1.4 tests")

  Feature("FR2.4: LLM Integration for Code Understanding") {
    Scenario("Chat service provides relevant code explanations", externalServiceTag) {
      Given("A configured chat service")
      val ragFactory      = RAGComponentFactoryImpl(config)
      val cacheService    = CacheService(ragFactory)
      val chatService     = ChatService(false, ContentService)
      val validUrl        = Some(URL(repoName).toAIServiceURL())
      val githubService   = GithubWrapperService(config, FetchingService())
      val ingestorService = IngestorService(cacheService, config, IngestionStrategyFactory)
      val pipeline = PipelineService(chatService, cacheService, ingestorService, githubService)

      When("Querying the chat service about code in the repository")
      val query    = "What functional programming patterns are used in this repository?"
      val response = pipeline.getAIService(validUrl).map(chatService.chat(query, _))

      Then("The response should be non-empty")
      response.fold(
        ex => fail(s"Expected success but got failure: $ex"),
        source =>
          // Convert Source[String, NotUsed] to String
          val result = Await.result(source.runWith(Sink.fold("")(_ + _)), 60.seconds)
          result should not be empty,
      )
    }

  }
