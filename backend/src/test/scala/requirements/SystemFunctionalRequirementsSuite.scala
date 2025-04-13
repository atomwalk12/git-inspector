package gitinsp.tests.requirements

import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.Sink
import com.typesafe.config.ConfigFactory
import gitinsp.domain.ChatService
import gitinsp.domain.IngestorService
import gitinsp.domain.Pipeline
import gitinsp.domain.models.Language
import gitinsp.domain.models.RepositoryWithLanguages
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

  /*
    Repository Cloning and Management (FR2.1)

    - Choice: Implement asynchronous repository cloning with progress tracking.
    - Rationale: Repository cloning can be time-consuming for large codebases and should not block the UI.
    - Success Criteria:
        - Repositories under 100MB clone successfully within 30 seconds
        - UI remains responsive (no blocking) during 100% of cloning operations
        - 100% of invalid repository URLs identified before cloning attempts
    - Implementation Considerations:
        - Use Github for extracting the repository code.
        - Implement caching mechanism for previously cloned repositories.
        - Add repository verification to ensure valid Git URLs.
   */
  Feature("FR2.1: Repository Cloning and Management") {
    Scenario("Clone a valid repository within acceptable time", externalServiceTag) {
      Given("A valid repository URL and language configuration")
      val githubService   = GithubWrapperService(config, FetchingService())
      val ragFactory      = RAGComponentFactoryImpl(config)
      val cacheService    = CacheService(ragFactory)
      val ingestorService = IngestorService(cacheService, config, IngestionStrategyFactory)

      And("A set of specific languages to filter")
      val validUrl  = URL(repoName)
      val languages = RepositoryWithLanguages.detectLanguages("scala,md")

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
      val languages     = RepositoryWithLanguages.detectLanguages("scala")

      When("Attempting to clone the repository")
      val result = githubService.buildRepository(invalidUrl, languages)

      Then("The operation should fail with appropriate error")
      result.isFailure shouldBe true
    }
  }

  /*
    Vector Database Generation for RAG (FR2.2)

    - Choice: Use a vector database for code embeddings.
      - Rationale: Since repositories can be large, it is necessary to split the data into chunks.
      - Success Criteria:
        - Generated embeddings cluster similar code types
        - Metadata correctly captures language and file type
      - Implementation Considerations:
        - Use Qdrant for caching code embeddings.
        - Utilize metadata to enhance search results relevance.
      - Related Requirements:
        - NFR-001 (Performance)
        - FR-002 (Book Catalog Management)
   */
  Feature("FR2.2: Vector Database Generation for RAG") {
    Scenario("Cache repository code embeddings in Qdrant", externalServiceTag) {
      Given("A configured pipeline and repository")
      val ragFactory      = RAGComponentFactoryImpl(config)
      val cacheService    = CacheService(ragFactory)
      val githubService   = GithubWrapperService(config, FetchingService())
      val chatService     = ChatService(false, ContentService)
      val ingestorService = IngestorService(cacheService, config, IngestionStrategyFactory)
      val pipeline        = Pipeline(chatService, cacheService, ingestorService, githubService)

      val validUrl  = URL("https://github.com/atomwalk12/deep-bridge-survey")
      val languages = RepositoryWithLanguages.detectLanguages("py,md")

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
    }
  }

  Feature("FR2.3: Vector Database Implementation") {
    Scenario("Vector database implementation") {
      info("This has already been shown in the FR1.2 and FR1.4 tests")
    }
  }

  /*
  - Choice: Integrate with Ollama and Langchain4J for language model capabilities.
    - Rationale: Local LLM deployment provides better privacy control and lower latency compared to cloud services.
    - Success Criteria:
      - Ollama integration successfully handles queries within tests without errors
    - Implementation Considerations:
      - Implement prompt engineering techniques to guide responses (i.e. conditional RAG, search by file type).
      - Design context management for large repositories (limit the amount of tokens being processed).
    - Related Requirements:
      - FR1.6 (Chat Functionality)
      - NFR1 (Performance)
      - NFR3 (Security)
   */
  Feature("FR2.4: LLM Integration for Code Understanding") {
    Scenario("Chat service provides relevant code explanations", externalServiceTag) {
      Given("A configured chat service")
      val ragFactory      = RAGComponentFactoryImpl(config)
      val cacheService    = CacheService(ragFactory)
      val chatService     = ChatService(false, ContentService)
      val validUrl        = Some(URL(repoName).toAIServiceURL())
      val githubService   = GithubWrapperService(config, FetchingService())
      val ingestorService = IngestorService(cacheService, config, IngestionStrategyFactory)
      val pipeline        = Pipeline(chatService, cacheService, ingestorService, githubService)

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
