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
import gitinsp.utils.Language
import dev.langchain4j.model.ollama.OllamaEmbeddingModel
import dev.langchain4j.data.segment.TextSegment
import dev.langchain4j.model.output.Response
import dev.langchain4j.data.embedding.Embedding

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
    org.mockito.Mockito.reset(mockConfig)

    // Config mock setup
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

  "The RAG Component Factory" should "be able to create a streaming chat model" in {
    val mockFactory = mock[DefaultRAGComponentFactory]
    val mockChat    = mock[OllamaStreamingChatModel]

    // Expectations
    when(mockFactory.createStreamingChatModel()).thenReturn(mockChat)

    // Mock objects
    val handler = mock[StreamingChatResponseHandler]

    // Chat with the model
    val chatModel = mockFactory.createStreamingChatModel()
    mockChat.chat("Hello, how are you?", handler)

    // Verify interactions
    verify(mockFactory).createStreamingChatModel()
    verify(mockChat).chat("Hello, how are you?", handler)
  }

  it should "be able to create an embedding model" in {
    val mockFactory        = mock[DefaultRAGComponentFactory]
    val mockEmbeddingModel = mock[OllamaEmbeddingModel]

    // Create test data
    val expectedArray     = Array(0.1f, 0.2f, 0.3f)
    val textSegment       = TextSegment.from("Hello, world!")
    val embeddingVector   = Embedding.from(expectedArray)
    val embeddingResponse = Response.from(embeddingVector)

    // Expectations
    when(mockFactory.createEmbeddingModel(Language.JAVA)).thenReturn(mockEmbeddingModel)
    when(mockEmbeddingModel.embed(textSegment)).thenReturn(embeddingResponse)

    // Attempt to embed code
    val embeddingModel = mockFactory.createEmbeddingModel(Language.JAVA)
    val result         = embeddingModel.embed(textSegment)

    // Verify interactions and result
    verify(mockFactory).createEmbeddingModel(Language.JAVA)
    verify(embeddingModel).embed(textSegment)
    result should be(embeddingResponse)

    // Verify the embedding values
    val embedding = result.content()
    embedding.vectorAsList().size() should be(3)
    embedding.vectorAsList().get(0).floatValue() should be(0.1f +- 0.001f)
    embedding.vectorAsList().get(1).floatValue() should be(0.2f +- 0.001f)
    embedding.vectorAsList().get(2).floatValue() should be(0.3f +- 0.001f)
  }
