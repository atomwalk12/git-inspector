package gitinsp.tests.pipe
import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Source
import com.typesafe.config.ConfigFactory
import dev.langchain4j.model.ollama.OllamaStreamingChatModel
import dev.langchain4j.rag.RetrievalAugmentor
import dev.langchain4j.service.TokenStream
import gitinsp.chatpipeline.RAGComponentFactory
import gitinsp.domain.ChatService
import gitinsp.domain.Pipeline
import gitinsp.infrastructure.CacheService
import gitinsp.infrastructure.ContentFormatter
import gitinsp.tests.ExternalService
import gitinsp.tests.Integration
import gitinsp.utils.Assistant
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.spy
import org.mockito.Mockito.verify
import org.mockito.Mockito.when
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar

import scala.concurrent.Await
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt

class PipelineTest extends AnyFlatSpec with Matchers with MockitoSugar:

  val config                                      = ConfigFactory.load()
  implicit val system: ActorSystem                = ActorSystem("pipeline-test-system", config)
  implicit val materializer: Materializer         = Materializer(system)
  implicit val executionContext: ExecutionContext = system.dispatcher

  "Pipeline" should "be able to run" taggedAs ExternalService in:
    // Setup classes
    val pipe = Pipeline(using system, materializer, executionContext)

    // Execute
    val source  = pipe.chat("Tell me a story about a cat!", "sample-repo")
    val future  = source.map(chunk => { println(chunk) }).runWith(Sink.seq)
    val results = Await.result(future, 20.seconds)

  "Pipeline" should "allow dependency injection for easier testing" taggedAs Integration in:
    // Create mocks
    val mockChatService  = mock[ChatService]
    val mockCacheService = mock[CacheService]
    val mockAssistant    = mock[Assistant]
    val mockResponse     = Source.single("Mocked response")

    // Set up mocks behavior
    when(mockCacheService.getAIService("test-repo")).thenReturn(mockAssistant)
    when(mockChatService.chat("Test query", mockAssistant)).thenReturn(mockResponse)

    // Execute the pipeline
    val pipe   = Pipeline(mockChatService, mockCacheService)
    val result = pipe.chat("Test query", "test-repo")

    // Verify
    result shouldBe mockResponse
    verify(mockCacheService).getAIService("test-repo")
    verify(mockChatService).chat("Test query", mockAssistant)

  it should "assert that the chat pipeline works" taggedAs Integration in:
    // Setup mocks and real classes
    val mockRAGFactory     = mock[RAGComponentFactory]
    val mockAssistant      = mock[Assistant]
    val mockStreamingModel = mock[OllamaStreamingChatModel]
    val mockAugmentor      = mock[RetrievalAugmentor]
    val mockTokenStream    = mock[TokenStream]
    val mockChatService    = spy(ChatService(false, ContentFormatter))
    val mockCacheService   = spy(CacheService(mockRAGFactory))
    val pipe               = Pipeline(mockChatService, mockCacheService)

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
    val result  = pipe.chat("Test query", "test-repo")
    val future  = result.runWith(Sink.seq)
    val results = Await.result(future, 5.seconds)

    // Verify
    verify(mockCacheService).getAIService("test-repo")
    results should contain theSameElementsAs List("Test response 1", "Test response 2")
