package gitinsp.tests.requirements

import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.Sink
import com.typesafe.config.ConfigFactory
import gitinsp.domain.ChatService
import gitinsp.domain.IngestorService
import gitinsp.domain.Pipeline
import gitinsp.domain.models.GitRepository
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

class NonFunctionalRequirementsSuite extends AnyFeatureSpec with GivenWhenThen with MockitoSugar
    with Matchers:

  // Setup test environment
  implicit val system: ActorSystem                = ActorSystem("business-requirements-test-system")
  implicit val materializer: Materializer         = Materializer(system)
  implicit val executionContext: ExecutionContext = system.dispatcher
  val config                                      = ConfigFactory.load()

  /*
    System Performance Optimization (NFR1)

    - Choice: Optimize performance for conversations with and without codebases.
      - Rationale: Users expect fast responses even with large codebases.
      - Validation Criteria:
        - Search queries return results in under 40 seconds for repositories up to 100MB
        - Embedding generation completes in under 30 seconds for repositories up to 100MB
        - Chat responses for simple search (without code context) arrives within 20 seconds for 90% of queries
      - Implementation Considerations:
        - Embeddings are stored in a persistent memory.
        - Data can be regenerated from the repository code.
   */
  Feature("NFR1: System Performance Optimization") {
    Scenario(
      "Search queries return results in under 40 seconds for repositories up to 100MB",
      externalServiceTag,
    ) {
      Given("A valid repository URL and language configuration")
      val githubService   = GithubWrapperService(config, FetchingService())
      val ragFactory      = RAGComponentFactoryImpl(config)
      val cacheService    = CacheService(ragFactory)
      val chatService     = ChatService(false, ContentService)
      val ingestorService = IngestorService(cacheService, config, IngestionStrategyFactory)
      val pipeline        = Pipeline(chatService, cacheService, ingestorService, githubService)

      And("A repository has been setup and indexed")
      val validUrl  = URL(repoName)
      val languages = GitRepository.detectLanguages("scala,md")
      val repository = githubService.buildRepository(
        validUrl,
        languages,
      ).getOrElse(fail("Failed to build repository"))
      ingestorService.ingest(repository)

      When("A search query is performed")
      val searchQuery = "What is this repository about?"
      val startTime   = System.currentTimeMillis()

      // Get the AI service and perform the search
      val url          = Some(validUrl.toAIServiceURL())
      val aiService    = pipeline.getAIService(url).getOrElse(fail("Failed to get AI service"))
      val searchResult = chatService.chat(searchQuery, aiService)

      // Check if at least one item is received from the stream within the time limit
      val firstResult    = Await.result(searchResult.take(1).runWith(Sink.headOption), 40.seconds)
      val processingTime = System.currentTimeMillis() - startTime

      Then("At least one response item should be received within the specified time")
      firstResult should not be empty
      processingTime should be <= 40000L
    }

    Scenario(
      "Embedding generation completes in under 30 seconds for repositories up to 100MB",
      externalServiceTag,
    ) {
      Given("A valid repository URL and language configuration")
      val githubService   = GithubWrapperService(config, FetchingService())
      val ragFactory      = RAGComponentFactoryImpl(config)
      val cacheService    = CacheService(ragFactory)
      val ingestorService = IngestorService(cacheService, config, IngestionStrategyFactory)

      And("A repository has been successfully built")
      val validUrl  = URL("https://github.com/atomwalk12/deep-bridge-survey")
      val languages = GitRepository.detectLanguages("scala,md")
      val repository = githubService.buildRepository(
        validUrl,
        languages,
      ).getOrElse(fail("Failed to build repository"))

      When("The repository code is ingested and embeddings are generated")
      val startTime      = System.currentTimeMillis()
      val ingestResult   = Try(ingestorService.ingest(repository))
      val processingTime = System.currentTimeMillis() - startTime

      Then("The ingestion should succeed within the specified time")
      ingestResult.isSuccess shouldBe true
      processingTime should be <= 30000L // 30 seconds max
    }

    Scenario("Chat responses for simple search arrive within 20 seconds", externalServiceTag) {
      Given("A configured chat service")
      val ragFactory      = RAGComponentFactoryImpl(config)
      val cacheService    = CacheService(ragFactory)
      val chatService     = ChatService(false, ContentService)
      val githubService   = GithubWrapperService(config, FetchingService())
      val ingestorService = IngestorService(cacheService, config, IngestionStrategyFactory)
      val pipeline        = Pipeline(chatService, cacheService, ingestorService, githubService)

      When("A simple chat query is performed")
      val query     = "Hi, what can you help me with?"
      val startTime = System.currentTimeMillis()
      // Using None for repository URL to test chat without code context
      val aiService      = pipeline.getAIService(None).getOrElse(fail("Failed to get AI service"))
      val response       = chatService.chat(query, aiService)
      val result         = Await.result(response.take(1).runWith(Sink.headOption), 20.seconds)
      val processingTime = System.currentTimeMillis() - startTime

      Then("The response should arrive within the specified time")
      result should not be empty
      processingTime should be <= 20000L // 20 seconds max
    }
  }

  /*
    System Usability Testing (NFR2)

    - Choice: The design of the interfaces should be evaluated by the users.
      - Rationale: This ensures that the system is usable, as evaluated through Usability Testing.
      - Success Criteria:
        - First-time users find the interface easy to use without assistance in >80% of cases (SUS)
        - 90% of users rate UI intuitiveness as "good" or "excellent" (SUS)
        - Keyboard shortcuts reduce interaction time by >30% for experienced users
      - Implementation Considerations:
        - Implement clean, intuitive UI. Rely on established patterns such as the Gradio UI components.
        - Update the user for the progress of the system.
   */
  Feature("NFR2: System Usability Testing") {
    Scenario("Usability metrics are tracked via System Usability Scale (SUS)") {
      info("This is a placeholder. SUS questionnaire to be conducted with users")
    }
  }

  /*
    User Interface Security (NFR3)

    - Choice: Implement input validation.
      - Rationale: User inputs (especially repository URLs and search queries) could contain malicious content.
      - Success Criteria:
        - 100% of malformed/malicious URLs rejected before processing
      - Implementation Considerations:
        - Validate repository URLs against known valid patterns.
        - Test against standard Github URLs.
   */
  Feature("NFR3: User Interface Security") {
    Scenario("Malformed repository URLs are rejected") {
      Given("A GithubWrapperService")
      val githubService = GithubWrapperService(config, FetchingService())

      When("Checking if malformed URLs are valid")
      val malformedUrls = List(
        "javascript:alert('XSS')",
        "data:text/html;base64,PHNjcmlwdD5hbGVydCgxKTwvc2NyaXB0Pg==",
        "file:///etc/passwd",
        "https://example.com;rm -rf /",
        "https://github.com/user/repo' OR '1'='1",
      )

      Then("All malformed URLs should be identified as invalid")
      malformedUrls.foreach {
        urlStr =>
          val isValidGithubUrl = Try(URL(urlStr)).isSuccess
          isValidGithubUrl shouldBe false
      }
    }
  }

  /*
    Embedding Visualization Requirement (NFR4)

    - Choice: Implement visualization tools for code embeddings analysis.
      - Rationale: Visualizing embeddings helps analyze and improve search quality.
      - Success Criteria:
        - Visualization correctly clusters similar code types
        - Report analysis identifies at least 3 insights from embedding visualization
      - Implementation Considerations:
        - Implement dimension reduction techniques (t-SNE, UMAP) for 2D visualization.
        - Used in the report to strengthen the analysis of the generated embeddings.
   */
  Feature("NFR4: Embedding Visualization") {
    Scenario("Embedding visualization for code analysis") {
      info("This is a placeholder. Embedding visualization to be included in the project report")
    }
  }
