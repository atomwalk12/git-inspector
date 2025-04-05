package gitinsp.tests.pipe
import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Source
import com.google.common.util.concurrent.Futures as F
import com.typesafe.config.Config
import dev.langchain4j.model.ollama.OllamaEmbeddingModel
import dev.langchain4j.model.ollama.OllamaStreamingChatModel
import dev.langchain4j.model.scoring.ScoringModel
import dev.langchain4j.rag.RetrievalAugmentor
import dev.langchain4j.service.TokenStream
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor
import gitinsp.chatpipeline.RAGComponentFactory
import gitinsp.domain.ChatService
import gitinsp.domain.IngestorService
import gitinsp.domain.Pipeline
import gitinsp.infrastructure.CacheService
import gitinsp.infrastructure.ContentFormatter
import gitinsp.infrastructure.strategies.IngestionStrategyFactory
import gitinsp.tests.ExternalService
import gitinsp.utils.Assistant
import gitinsp.utils.GitDocument
import gitinsp.utils.GitRepository
import gitinsp.utils.IndexName
import gitinsp.utils.Language
import io.qdrant.client.QdrantClient
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.spy
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar

import java.util.Collections as C

import scala.concurrent.Await
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt

class PipelineTest extends AnyFlatSpec with Matchers with MockitoSugar with BeforeAndAfterAll:

  val config                                      = mock[Config]
  implicit val system: ActorSystem                = ActorSystem("pipeline-test-system")
  implicit val materializer: Materializer         = Materializer(system)
  implicit val executionContext: ExecutionContext = system.dispatcher

  val mockRAGFactory      = mock[RAGComponentFactory]
  val mockIngestorService = mock[IngestorService]
  val mockChatService     = spy(ChatService(false, ContentFormatter))
  val mockCacheService    = spy(CacheService(mockRAGFactory))

  override def beforeAll(): Unit =
    when(config.getString("gitinsp.ollama.url")).thenReturn("http://localhost:11434")
    when(config.getString("gitinsp.code-embedding.model")).thenReturn("nomic-embed-text")
    when(config.getString("gitinsp.text-embedding.model")).thenReturn("nomic-embed-text")
    when(config.getString("gitinsp.models.default-model")).thenReturn("llama3.3")
    when(config.getString("gitinsp.rag.model")).thenReturn("llama3.3")
    when(config.getInt("gitinsp.code-embedding.chunk-size")).thenReturn(1000)
    when(config.getInt("gitinsp.text-embedding.chunk-size")).thenReturn(1000)
    when(config.getInt("gitinsp.code-embedding.chunk-overlap")).thenReturn(200)
    when(config.getInt("gitinsp.text-embedding.chunk-overlap")).thenReturn(200)

  "Pipeline with external services" should "be able to execute" taggedAs ExternalService in:
    // Setup classes
    val pipe = Pipeline(using system, materializer, executionContext)

    // Execute
    val source  = pipe.chat("Hi!", None)
    val future  = source.map(println).runWith(Sink.seq)
    val results = Await.result(future, 2.minutes)

  it should "be able to execute with content retrieval" taggedAs ExternalService in:
    // Setup classes
    val pipe  = Pipeline(using system, materializer, executionContext)
    val index = IndexName("test-repo-py", Language.PYTHON)

    // Execute
    val source  = pipe.chat("Hi!", Some(index))
    val future  = source.map(println).runWith(Sink.seq)
    val results = Await.result(future, 2.minutes)

  it should "be able to process documents" taggedAs ExternalService in:
    // Setup classes
    val pipeWithExternalServices = Pipeline(using system, materializer, executionContext)

    // Setup data
    val languages = GitRepository.detectLanguages("scala,md,py")
    val doc1      = GitDocument("def test()", Language.SCALA, "test.scala")
    val doc2      = GitDocument("# Hello, world!", Language.MARKDOWN, "test.md")
    val doc3      = GitDocument("print('Hello, world!')", Language.PYTHON, "test.py")
    val docs      = List(doc1, doc1, doc2, doc3)
    val repo      = GitRepository("test-repo", languages, docs)

    // Execute
    val source = pipeWithExternalServices.regenerateIndex(repo)

  "Pipeline" should "allow dependency injection for easier testing" in:
    // Create mocks
    val mockChatService  = mock[ChatService]
    val mockCacheService = mock[CacheService]
    val mockAssistant    = mock[Assistant]
    val mockResponse     = Source.single("Mocked response")

    // Set up mocks behavior
    when(mockCacheService.getAIService(None)).thenReturn(mockAssistant)
    when(mockChatService.chat("Test query", mockAssistant)).thenReturn(mockResponse)

    // Execute the pipeline
    val pipe   = Pipeline(mockChatService, mockCacheService, mockIngestorService)
    val index  = None
    val result = pipe.chat("Test query", index)

    // Verify
    result shouldBe mockResponse
    verify(mockCacheService).getAIService(None)
    verify(mockChatService).chat("Test query", mockAssistant)

  it should "assert that the chat pipeline works" in:
    // Setup mocks and real classes
    val mockAssistant      = mock[Assistant]
    val mockStreamingModel = mock[OllamaStreamingChatModel]
    val mockAugmentor      = mock[RetrievalAugmentor]
    val mockTokenStream    = mock[TokenStream]

    val pipe = Pipeline(mockChatService, mockCacheService, mockIngestorService)

    // Setup data
    val mockSource = Source(List("Test response 1", "Test response 2"))

    // Setup behaviour
    when(mockTokenStream.onRetrieved(any())).thenReturn(mockTokenStream)
    when(mockTokenStream.onPartialResponse(any())).thenReturn(mockTokenStream)
    when(mockTokenStream.onCompleteResponse(any())).thenReturn(mockTokenStream)
    when(mockTokenStream.onError(any())).thenReturn(mockTokenStream)
    when(mockAssistant.chat(anyString())).thenReturn(mockTokenStream)
    doReturn(mockAssistant).when(mockRAGFactory).createAssistant(any(), any())
    doReturn(mockSource).when(mockChatService).chat(any(), any())

    // Execute
    val index   = IndexName("test-repo", Language.SCALA)
    val result  = pipe.chat("Test query", Some(index))
    val future  = result.runWith(Sink.seq)
    val results = Await.result(future, 5.seconds)

    // Verify
    verify(mockCacheService).getAIService(Some(index))
    results should contain theSameElementsAs List("Test response 1", "Test response 2")

  it should "ingest the repository" in:
    // Mocks
    val mockQdrantClient   = mock[QdrantClient]
    val scoringModel       = mock[ScoringModel]
    val mockEmbeddingModel = mock[OllamaEmbeddingModel]
    val mockIngestor       = mock[EmbeddingStoreIngestor]
    val mockFactory        = spy(RAGComponentFactory(config))

    // Setup mocks
    doReturn(mockIngestor).when(mockCacheService).getIngestor(any, any)
    doReturn(F.immediateFuture(C.emptyList[String]())).when(mockQdrantClient).listCollectionsAsync()
    doReturn(mockQdrantClient).when(mockFactory).createQdrantClient()
    doReturn(scoringModel).when(mockFactory).createScoringModel()
    doReturn(mockEmbeddingModel).when(mockFactory).createTextEmbeddingModel()
    doReturn(mockEmbeddingModel).when(mockFactory).createCodeEmbeddingModel()
    doReturn(mockIngestor).when(mockFactory).createIngestor(any, any, any, any)

    when(mockCacheService.qdrantClient).thenReturn(mockQdrantClient)
    when(mockFactory.createScoringModel()).thenReturn(scoringModel)

    // Setup data
    val ingestorService = IngestorService(mockCacheService, config)

    // Setup data
    val pipe      = Pipeline(mockChatService, mockCacheService, ingestorService)
    val languages = GitRepository.detectLanguages("scala,md")
    val doc1      = GitDocument("def test()", Language.SCALA, "test.scala")
    val doc2      = GitDocument("# Hello, world!", Language.MARKDOWN, "test.md")
    val docs      = List(doc1, doc2, doc1, doc2)
    val repo      = GitRepository("test-repo", languages, docs)

    // Execute
    pipe.regenerateIndex(repo)

    // Verify
    repo.indexNames.foreach {
      index =>
        val strategy = IngestionStrategyFactory.createStrategy("default", index.language, config)

        import gitinsp.domain.IngestorServiceExtensions.ingest
        verify(mockIngestor, times(2)).ingest(repo, index.language)
    }
