package gitinsp.tests.chat
import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Source
import com.typesafe.config.ConfigFactory
import dev.langchain4j.service.TokenStream
import gitinsp.chatpipeline.RAGComponentFactory
import gitinsp.domain.ChatService
import gitinsp.infrastructure.ContentFormatter
import gitinsp.tests.HandleErrors
import gitinsp.utils.Assistant
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.spy
import org.mockito.Mockito.verify
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterAll
import org.scalatest.OptionValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar

import scala.concurrent.Await
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt

class ChatServiceSuite extends AnyFlatSpec with Matchers with MockitoSugar with BeforeAndAfterAll
    with OptionValues:

  val config                                      = ConfigFactory.load()
  implicit val system: ActorSystem                = ActorSystem("pipeline-test-system", config)
  implicit val materializer: Materializer         = Materializer(system)
  implicit val executionContext: ExecutionContext = system.dispatcher

  val mockTokenStream = mock[TokenStream]
  override def beforeAll(): Unit =
    super.beforeAll()

    // Configure token stream for all tests
    when(mockTokenStream.onRetrieved(any())).thenReturn(mockTokenStream)
    when(mockTokenStream.onPartialResponse(any())).thenReturn(mockTokenStream)
    when(mockTokenStream.onCompleteResponse(any())).thenReturn(mockTokenStream)
    when(mockTokenStream.onError(any())).thenReturn(mockTokenStream)

  "ChatService" should "enable interaction with the assistant" in:
    // Setup mocks and real classes
    val mockRAGFactory = mock[RAGComponentFactory]
    val mockAssistant  = mock[Assistant]

    val mockChatService = spy(ChatService(false, ContentFormatter))

    // Setup data
    val mockSource = Source(List("Test response 1", "Test response 2"))

    // Setup behaviour
    when(mockAssistant.chat(anyString())).thenReturn(mockTokenStream)
    doReturn(mockAssistant).when(mockRAGFactory).createAssistant(any(), any())
    doReturn(mockSource).when(mockChatService).chat(any(), any())

    // Execute
    val result  = mockChatService.chat("Test query", mockAssistant)
    val future  = result.runWith(Sink.seq)
    val results = Await.result(future, 5.seconds)

    // Verify
    verify(mockChatService).chat(anyString(), any())
    results should contain theSameElementsAs List("Test response 1", "Test response 2")

  it should "handle errors" taggedAs HandleErrors in:
    // Create mock Assistant
    val mockAssistant    = mock[Assistant]
    val mockErrorService = spy(ChatService())

    // Create data
    val errorSource = Source.failed(new RuntimeException("Test error"))

    // Setup behaviour
    doReturn(errorSource).when(mockErrorService).chat(any(), any())

    // Execute
    val result = mockErrorService.chat("query", mockAssistant)
    val future = result.runWith(Sink.seq)

    // Verify the future fails
    assertThrows[RuntimeException] { Await.result(future, 5.seconds) }
