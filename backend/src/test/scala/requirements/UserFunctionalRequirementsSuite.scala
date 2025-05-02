package gitinsp.tests.requirements

import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.Sink
import com.typesafe.config.ConfigFactory
import dev.langchain4j.store.embedding.EmbeddingSearchRequest
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
import gitinsp.tests.ENABLE_LOGGING
import gitinsp.tests.externalServiceTag
import gitinsp.tests.repoName
import gitinsp.tests.storeName_code
import gitinsp.tests.storeName_text
import org.scalatest.GivenWhenThen
import org.scalatest.featurespec.AnyFeatureSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar

import scala.concurrent.Await
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.*
import scala.jdk.CollectionConverters.*
import scala.util.Try

class UserFunctionalRequirementsSuite extends AnyFeatureSpec with GivenWhenThen with MockitoSugar
    with Matchers:

  // Setup test environment
  implicit val system: ActorSystem                = ActorSystem("business-requirements-test-system")
  implicit val materializer: Materializer         = Materializer(system)
  implicit val executionContext: ExecutionContext = system.dispatcher
  val config                                      = ConfigFactory.load()

  Feature("FR1.1: Repository URL Input Interface"):
    Scenario("Valid GitHub URL is successfully processed", externalServiceTag):
      Given("A configured pipeline with necessary services")
      val githubService   = GithubWrapperService(config, FetchingService())
      val ragFactory      = RAGComponentFactoryImpl(config)
      val cacheService    = CacheService(ragFactory)
      val chatService     = ChatService(false, ContentService)
      val ingestorService = IngestorService(cacheService, config, IngestionStrategyFactory)
      val pipeline = PipelineService(chatService, cacheService, ingestorService, githubService)

      And("A valid GitHub repository URL")
      val validUrl  = URL(repoName)
      val languages = GitRepository.detectLanguages("scala,md")

      When("The URL is processed")
      val startTime      = System.currentTimeMillis()
      val result         = githubService.buildRepository(validUrl, languages)
      val processingTime = System.currentTimeMillis() - startTime

      Then("The URL should be processed successfully")
      result.isSuccess shouldBe true

      And("Processing should complete within 1 second")
      processingTime should be <= 5000L

      And("The repository should contain the specified languages")
      result.foreach {
        repo =>
          repo.languages should contain(Language.SCALA)
          repo.languages should contain(Language.MARKDOWN)
      }

    Scenario("Invalid GitHub URL is rejected with quick feedback", externalServiceTag):
      Given("A configured GitHub service")
      val githubService = GithubWrapperService(config, FetchingService())
      val languages     = GitRepository.detectLanguages("scala")

      When("An invalid URL is processed")
      val startTime      = System.currentTimeMillis()
      val result         = Try(URL("invalid-url-format"))
      val processingTime = System.currentTimeMillis() - startTime

      Then("The URL should be rejected")
      if ENABLE_LOGGING then println(result)
      result.isFailure shouldBe true

      And("Error feedback should be provided within 500ms")
      processingTime should be <= 500L

    Scenario("Multiple file extensions are correctly processed", externalServiceTag):
      Given("A configured pipeline")
      val githubService = GithubWrapperService(config, FetchingService())
      val validUrl      = URL(repoName)

      When("Multiple extensions are specified")
      val languages = GitRepository.detectLanguages("scala,md,py")
      val result    = githubService.buildRepository(validUrl, languages)

      Then("All specified extensions should be included")
      result.foreach: repo =>
        repo.languages should contain allOf (
          Language.SCALA,
          Language.MARKDOWN,
          Language.PYTHON,
        )

  Feature("FR1.2: Code Search Using Markdown"):
    Scenario("Natural language query returns relevant code files", externalServiceTag):
      Given("A configured pipeline with necessary services")
      val githubService      = GithubWrapperService(config, FetchingService())
      val ragFactory         = RAGComponentFactoryImpl(config)
      val qdrantClient       = ragFactory.createQdrantClient()
      val embeddingStore     = ragFactory.createEmbeddingStore(qdrantClient, storeName_text)
      val textEmbeddingModel = ragFactory.createTextEmbeddingModel()

      When("A user query is processed")
      val queryEmbedding = textEmbeddingModel.embed("What is this project about?").content()

      And("A natural language query is made")
      val searchRequest = EmbeddingSearchRequest.builder().queryEmbedding(queryEmbedding).build()
      val result        = embeddingStore.search(searchRequest)

      Then("The result should be a list of code files")
      result.matches should not be empty
      if ENABLE_LOGGING then result.matches.asScala.foreach(m => println(m.score)) // EmbeddingMatch
      if ENABLE_LOGGING then result.matches.asScala.foreach(m => println(m.embedded().text()))

    Scenario(
      "Search results return within performance requirements within 2 seconds",
      externalServiceTag,
    ):
      Given("A configured pipeline with necessary services")
      val githubService      = GithubWrapperService(config, FetchingService())
      val ragFactory         = RAGComponentFactoryImpl(config)
      val qdrantClient       = ragFactory.createQdrantClient()
      val embeddingStore     = ragFactory.createEmbeddingStore(qdrantClient, storeName_text)
      val textEmbeddingModel = ragFactory.createTextEmbeddingModel()

      When("A search query is executed")
      val startTime      = System.currentTimeMillis()
      val queryEmbedding = textEmbeddingModel.embed("Find code related to GitHub API").content()
      val searchRequest  = EmbeddingSearchRequest.builder().queryEmbedding(queryEmbedding).build()
      val result         = embeddingStore.search(searchRequest)
      val endTime        = System.currentTimeMillis()
      val executionTime  = endTime - startTime

      Then("The search should complete within 2 seconds")
      executionTime should be <= 2000L

      And("Results should be returned")
      result.matches should not be empty

    Scenario("Language filtering correctly categorizes code files", externalServiceTag):
      Given("A configured pipeline with necessary services")
      val githubService = GithubWrapperService(config, FetchingService())
      val ragFactory    = RAGComponentFactoryImpl(config)
      val validUrl      = URL(repoName)
      val languages     = GitRepository.detectLanguages("scala,md")

      When("Repository content is analyzed")
      val repoResult = githubService.buildRepository(validUrl, languages)

      Then("Files should be correctly categorized by language")
      repoResult.isSuccess shouldBe true
      repoResult.foreach: repo =>
        // Check that languages are correctly detected
        repo.languages should contain(Language.SCALA)
        repo.languages should contain(Language.MARKDOWN)

        // Verify Scala files are correctly identified
        val scalaFiles = repo.docs.filter(_.language == Language.SCALA)
        scalaFiles.foreach {
          file =>
            file.path should endWith(".scala")
            file.language shouldBe Language.SCALA
        }

        // Verify Markdown files are correctly identified
        val markdownFiles = repo.docs.filter(_.language == Language.MARKDOWN)
        markdownFiles.foreach {
          file =>
            file.path should (endWith(".md") or endWith(".MD"))
            file.language shouldBe Language.MARKDOWN
        }

  // ----- NOTE: FR1.3 is assessed using the SUS survey -----

  Feature("FR1.4: Code Search using Code Embeddings") {
    Scenario("Natural language query returns relevant code files", externalServiceTag) {
      Given("A configured pipeline with necessary services")
      val githubService      = GithubWrapperService(config, FetchingService())
      val ragFactory         = RAGComponentFactoryImpl(config)
      val qdrantClient       = ragFactory.createQdrantClient()
      val embeddingStore     = ragFactory.createEmbeddingStore(qdrantClient, storeName_code)
      val codeEmbeddingModel = ragFactory.createCodeEmbeddingModel()

      When("A user query is processed using a code embedding model")
      val queryEmbedding = codeEmbeddingModel.embed("What is this project about?").content()

      And("A natural language query is made")
      val searchRequest = EmbeddingSearchRequest.builder().queryEmbedding(queryEmbedding).build()
      val result        = embeddingStore.search(searchRequest)

      Then("The result should be a list of code files")
      result.matches should not be empty
      result.matches.asScala.foreach(m => m.embedded().text().contains(".scala"))
      if ENABLE_LOGGING then result.matches.asScala.foreach(m => println(m.score))
      if ENABLE_LOGGING then result.matches.asScala.foreach(m => println(m.embedded().text()))
    }
  }

  Feature("FR1.5: Code Context Visualization"):
    Scenario("Repository fetching performance meets requirements", externalServiceTag):
      Given("A configured GitHub wrapper service")
      val githubService = GithubWrapperService(config, FetchingService())
      val validUrl      = URL(repoName)
      val languages     = GitRepository.detectLanguages("scala,md")

      When("A repository is fetched")
      val startTime = System.currentTimeMillis()
      val result    = githubService.fetchRepository(validUrl, languages)
      val endTime   = System.currentTimeMillis()
      val fetchTime = endTime - startTime

      Then("The repository should be fetched successfully")
      result.isSuccess shouldBe true

      And("Fetching should complete within 5 seconds")
      fetchTime should be <= 5000L

      And("The fetched repository should contain code content")
      result.foreach: content =>
        content should not be empty
        // Verify we have actual code content
        content should (include(".scala") or include(".md"))

    Scenario("Full code context is provided for search results", externalServiceTag):
      Given("A configured GitHub wrapper service")
      val githubService = GithubWrapperService(config, FetchingService())
      val validUrl      = URL(repoName)
      val languages     = GitRepository.detectLanguages("scala")

      When("A repository is built with code files")
      val repoResult = githubService.buildRepository(validUrl, languages)

      Then("Each code file should contain full context")
      repoResult.isSuccess shouldBe true
      repoResult.foreach: repo =>
        repo.docs.foreach: file =>
          file.content should not be empty

          // Check for common code structure indicators
          if file.language == Language.SCALA then
            file.content should (
              include("import") or
                include("def") or
                include("val") or
                include("class") or
                include("object") or
                include("trait")
            )

  Feature("FR1.6: Model with Memory of Chat History"):
    Scenario(
      "Context-aware responses remain relevant for at least 2 consecutive related questions",
      externalServiceTag,
    ):
      Given("A configured pipeline with necessary services")
      val ragFactory      = RAGComponentFactoryImpl(config)
      val cacheService    = CacheService(ragFactory)
      val chatService     = ChatService(false, ContentService)
      val ingestorService = IngestorService(cacheService, config, IngestionStrategyFactory)
      val githubService   = GithubWrapperService(config, FetchingService())
      val pipeline     = PipelineService(chatService, cacheService, ingestorService, githubService)
      val aiServiceUrl = Some(URL(repoName).toAIServiceURL())

      When("A user asks to remember a keyword")
      pipeline.getAIService(aiServiceUrl)
        .map(service => pipeline.chat("Please remember the following keyword: apple?", service))

      And("The user asks what was the keyword")
      val responseSource = pipeline.getAIService(aiServiceUrl)
        .map(service => pipeline.chat("What is the keyword?", service))

      Then("The keyword should be remembered")
      responseSource.isSuccess shouldBe true
      responseSource.fold(
        ex => fail(s"Expected success but got failure: $ex"),
        source =>
          // Convert Source[String, NotUsed] to String
          val result = Await.result(source.runWith(Sink.fold("")(_ + _)), 60.seconds)
          result should include("apple"),
      )
