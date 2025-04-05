package gitinsp.tests.cacheservice

import com.typesafe.config.Config
import dev.langchain4j.model.scoring.ScoringModel
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor
import gitinsp.chatpipeline.RAGComponentFactory
import gitinsp.infrastructure.CacheService
import gitinsp.infrastructure.strategies.IngestionStrategyFactory as ISF
import gitinsp.utils.IndexName
import gitinsp.utils.Language
import io.qdrant.client.QdrantClient
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.spy
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar

import scala.concurrent.ExecutionContext

class CacheServiceSuite
    extends AnyFlatSpec
    with Matchers
    with BeforeAndAfterAll
    with MockitoSugar
    with ScalaFutures:

  // Create a dedicated execution context for tests
  implicit val ec: ExecutionContext = ExecutionContext.fromExecutor(
    java.util.concurrent.Executors.newFixedThreadPool(4),
  )

  // Create the mocks
  val config           = mock[Config]
  val mockFactory      = spy(RAGComponentFactory(config))
  val mockQdrantClient = mock[QdrantClient]
  val scoringModel     = mock[ScoringModel]
  override def beforeAll(): Unit =
    // Setup the mock to return our mock client
    // when(mockFactory.createQdrantClient()).thenReturn(mockQdrantClient)
    doReturn(mockQdrantClient).when(mockFactory).createQdrantClient()
    doReturn(scoringModel).when(mockFactory).createScoringModel()
    when(config.getString("gitinsp.ollama.url")).thenReturn("http://localhost:11434")
    when(config.getString("gitinsp.code-embedding.model")).thenReturn("nomic-embed-text")
    when(config.getString("gitinsp.text-embedding.model")).thenReturn("nomic-embed-text")
    when(config.getString("gitinsp.models.default-model")).thenReturn("llama3.3")
    when(config.getString("gitinsp.rag.model")).thenReturn("llama3.3")
    when(config.getInt("gitinsp.chat.memory")).thenReturn(10)

  "CacheService" should "run without any exceptions" in:
    // Get the CacheService implementation
    val cacheService = CacheService(mockFactory)

    // Now test the service with mocked dependencies
    val indexName = IndexName("test-repository", Language.SCALA)
    val aiService = cacheService.getAIService(Some(indexName))
    noException should be thrownBy aiService

  it should "return the same AI service instance for the same repository name" in:
    // Data
    val cacheService = CacheService(mockFactory)

    // Execute
    val indexName  = IndexName("test-repository", Language.SCALA)
    val aiService1 = cacheService.getAIService(Some(indexName))
    val aiService2 = cacheService.getAIService(Some(indexName))

    // Verify
    aiService1 should be theSameInstanceAs aiService2

  it should "create an ingestor for a specific language" in:
    when(mockFactory.createQdrantClient()).thenReturn(mockQdrantClient)
    // Data
    val cacheService      = CacheService(mockFactory)
    val ingestionStrategy = ISF.createStrategy("default", Language.SCALA, config)
    val indexName         = IndexName("test-repository", Language.SCALA)

    // Execute
    val ingestor = cacheService.getIngestor(indexName, ingestionStrategy)

    // Verify
    ingestor shouldBe a[EmbeddingStoreIngestor]

  it should "create an ingestor for markdown" in:
    when(mockFactory.createQdrantClient()).thenReturn(mockQdrantClient)
    // Data
    val cacheService      = CacheService(mockFactory)
    val ingestionStrategy = ISF.createStrategy("default", Language.MARKDOWN, config)
    val indexName         = IndexName("test-repository", Language.MARKDOWN)

    // Execute
    val ingestor = cacheService.getIngestor(indexName, ingestionStrategy)

    // Verify
    ingestor shouldBe a[EmbeddingStoreIngestor]
