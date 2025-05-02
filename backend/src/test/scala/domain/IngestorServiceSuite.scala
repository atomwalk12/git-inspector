package gitinsp.tests.domain

import com.google.common.util.concurrent.Futures as F
import com.typesafe.config.ConfigFactory
import dev.langchain4j.model.ollama.OllamaEmbeddingModel
import dev.langchain4j.model.scoring.ScoringModel
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor
import gitinsp.domain.IngestorService
import gitinsp.domain.models.CodeFile
import gitinsp.domain.models.GitRepository
import gitinsp.domain.models.IngestorServiceExtensions.ingest
import gitinsp.domain.models.Language
import gitinsp.domain.models.URL
import gitinsp.infrastructure.CacheService
import gitinsp.infrastructure.factories.RAGComponentFactory
import gitinsp.infrastructure.strategies.IngestionStrategyFactory
import io.qdrant.client.QdrantClient
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.spy
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar

import java.util.Collections as C

import scala.concurrent.ExecutionContext
import scala.util.Success

import GitRepository.detectLanguages

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
  val config             = ConfigFactory.load()
  val mockFactory        = spy(RAGComponentFactory(config))
  val mockQdrantClient   = mock[QdrantClient]
  val scoringModel       = mock[ScoringModel]
  val mockEmbeddingModel = mock[OllamaEmbeddingModel]
  val mockIngestor       = mock[EmbeddingStoreIngestor]

  override def beforeAll(): Unit =
    // Setup behavior
    doReturn(mockQdrantClient).when(mockFactory).createQdrantClient()
    doReturn(F.immediateFuture(C.emptyList[String]())).when(mockQdrantClient).listCollectionsAsync()
    doReturn(scoringModel).when(mockFactory).createScoringModel()
    doReturn(mockEmbeddingModel).when(mockFactory).createTextEmbeddingModel()
    doReturn(mockEmbeddingModel).when(mockFactory).createCodeEmbeddingModel()
    doReturn(mockIngestor).when(mockFactory).createIngestor(any, any, any, any)
    doReturn(Success(())).when(mockFactory).createCollection(any(), any(), any())

  "IngestorService" should "create an ingestor for text and code" in:
    // Data setup
    val url        = URL("https://github.com/gitinsp/gitinsp")
    val languages  = "py,md,scala,py"
    val doc1       = CodeFile("Hello, world!", Language.MARKDOWN, "README.md", 1000, 100)
    val doc2       = CodeFile("Hello, world!", Language.SCALA, "README.scala", 1000, 100)
    val doc3       = CodeFile("Hello, world!", Language.PYTHON, "README.py", 1000, 100)
    val docs       = List(doc1, doc2, doc3)
    val repository = GitRepository(url, detectLanguages(languages), docs)

    // Mocks
    val mockCacheService = spy(CacheService(mockFactory))

    // Execute
    val ingestorService = IngestorService(mockCacheService, config, IngestionStrategyFactory)
    ingestorService.ingest(repository)

    // Execute and verify
    repository.languages.zip(repository.indexNames).foreach {
      case (language, index) =>
        val strategy = IngestionStrategyFactory.createStrategy("default", language, config)
        verify(mockIngestor, times(1)).ingest(repository, language)
    }
