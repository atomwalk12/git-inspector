package gitinsp.tests

import com.typesafe.config.Config
import dev.langchain4j.data.embedding.Embedding
import dev.langchain4j.data.segment.TextSegment
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler
import dev.langchain4j.model.ollama.OllamaChatModel
import dev.langchain4j.model.ollama.OllamaEmbeddingModel
import dev.langchain4j.model.ollama.OllamaStreamingChatModel
import dev.langchain4j.model.output.Response
import gitinsp.analysis.*
import gitinsp.chatpipeline.ConditionalQueryStrategy
import gitinsp.chatpipeline.DefaultQueryStrategy
import gitinsp.chatpipeline.QueryRoutingStrategyFactory
import gitinsp.chatpipeline.RAGComponentFactoryImpl
import gitinsp.utils.Language
import gitinsp.utils.StreamingAssistant
import org.mockito.Mockito.verify
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import org.scalatest.Tag
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar

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
    when(mockConfig.getString("tinygpt.models.default-model")).thenReturn("llama3.1")
  }

  "The Analysis Context" should "be able to analyze code" in:
    val analysis = AnalysisContext.withCodeAnalysisStrategy(StreamingAssistant())
    analysis.strategy.map(_.strategyName) should be(Some("Code Analysis"))

  it should "be able to analyze natural language" in:
    val analysis = AnalysisContext.withNaturalLanguageStrategy(StreamingAssistant())
    analysis.strategy.map(_.strategyName) should be(Some("Markdown Analysis"))

  "The RAG Component Factory" should "be able to create a streaming chat model" in:
    val mockFactory = mock[RAGComponentFactoryImpl]
    val mockChat    = mock[OllamaStreamingChatModel]

    // Expectations
    when(mockFactory.createStreamingChatModel()).thenReturn(mockChat)

    // Mock objects
    val handler = mock[StreamingChatResponseHandler]

    // Action: Chat with the model
    val chatModel = mockFactory.createStreamingChatModel()
    mockChat.chat("Hello, how are you?", handler)

    // Verify interactions
    verify(mockFactory).createStreamingChatModel()
    verify(mockChat).chat("Hello, how are you?", handler)

  it should "be able to create an embedding model" in:
    val mockFactory        = mock[RAGComponentFactoryImpl]
    val mockEmbeddingModel = mock[OllamaEmbeddingModel]

    // Create test data
    val expectedArray     = Array(0.1f, 0.2f, 0.3f)
    val textSegment       = TextSegment.from("Hello, world!")
    val embeddingVector   = Embedding.from(expectedArray)
    val embeddingResponse = Response.from(embeddingVector)

    // Expectations
    when(mockFactory.createEmbeddingModel(Language.JAVA)).thenReturn(mockEmbeddingModel)
    when(mockEmbeddingModel.embed(textSegment)).thenReturn(embeddingResponse)

    // Action: Attempt to embed code
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

  it should "be able to create a query router" in:
    val mockChatModel = mock[OllamaChatModel]

    val condStrategy = QueryRoutingStrategyFactory.createStrategy("conditional", mockChatModel)
    condStrategy shouldBe a[ConditionalQueryStrategy]

    val defaultStrategy = QueryRoutingStrategyFactory.createStrategy("default", mockChatModel)
    defaultStrategy shouldBe a[DefaultQueryStrategy]

    val fallbackStrategy = QueryRoutingStrategyFactory.createStrategy("unknown", mockChatModel)
    fallbackStrategy shouldBe a[DefaultQueryStrategy]

    val uppercaseStrategy = QueryRoutingStrategyFactory.createStrategy("CONDITIONAL", mockChatModel)
    uppercaseStrategy shouldBe a[ConditionalQueryStrategy]

    val mixedCaseStrategy = QueryRoutingStrategyFactory.createStrategy("Default", mockChatModel)
    mixedCaseStrategy shouldBe a[DefaultQueryStrategy]
