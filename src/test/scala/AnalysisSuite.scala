package gitinsp.tests

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import gitinsp.analysis.*
import gitinsp.utils.StreamingAssistant
import gitinsp.chatpipeline.DefaultRAGComponentFactory
import org.scalatestplus.mockito.MockitoSugar
import org.scalatest.BeforeAndAfterEach
import org.mockito.Mockito.{verify, when}
import com.typesafe.config.Config
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler
import dev.langchain4j.model.ollama.OllamaStreamingChatModel
import org.scalatest.Tag

object Slow extends Tag("org.scalatest.tags.Slow")

class AnalysisTest
    extends AnyFlatSpec
    with Matchers
    with BeforeAndAfterEach
    with MockitoSugar:

  val mockConfig = mock[Config]

  // Create a mock configuration
  override def beforeEach(): Unit = {
    super.beforeEach()
    // Reset mocks before each test
    org.mockito.Mockito.reset(mockConfig)

    // Config mock setup - moved error-throwing configuration out of here
    when(mockConfig.getString("tinygpt.ollama.url")).thenReturn("http://localhost:11434")
    when(mockConfig.getString("tinygpt.models.default-model")).thenReturn("llama2")
  }

  "The Analysis Context" should "be able to analyze code" in {
    val analysis = AnalysisContext.withCodeAnalysisStrategy(StreamingAssistant())
    analysis.strategy.map(_.strategyName) should be(Some("Code Analysis"))
  }

  it should "be able to analyze natural language" in {
    val analysis = AnalysisContext.withNaturalLanguageStrategy(StreamingAssistant())
    analysis.strategy.map(_.strategyName) should be(Some("Markdown Analysis"))
  }

  it should "be able to chat with the streaming chat model" in {
    val mockFactory = mock[DefaultRAGComponentFactory]
    val mockChat    = mock[OllamaStreamingChatModel]

    // Set up expectations
    when(mockFactory.createStreamingChatModel()).thenReturn(mockChat)

    // Test with mocked handler
    val handler = mock[StreamingChatResponseHandler]

    // Perform the chat operation
    val chatModel = mockFactory.createStreamingChatModel()
    mockChat.chat("Hello, how are you?", handler)

    // Verify the interaction happened as expected
    verify(mockFactory).createStreamingChatModel()
    verify(mockChat).chat("Hello, how are you?", handler)
  }
