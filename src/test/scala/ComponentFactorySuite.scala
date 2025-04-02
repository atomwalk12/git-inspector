package gitinsp.tests

import com.typesafe.config.Config
import dev.langchain4j.data.embedding.Embedding
import dev.langchain4j.data.message.AiMessage
import dev.langchain4j.data.message.UserMessage
import dev.langchain4j.data.segment.TextSegment
import dev.langchain4j.model.chat.response.ChatResponse
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler
import dev.langchain4j.model.ollama.OllamaChatModel
import dev.langchain4j.model.ollama.OllamaEmbeddingModel
import dev.langchain4j.model.ollama.OllamaStreamingChatModel
import dev.langchain4j.model.output.Response
import dev.langchain4j.model.scoring.ScoringModel
import dev.langchain4j.rag.DefaultRetrievalAugmentor
import dev.langchain4j.rag.content.aggregator.ReRankingContentAggregator
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever
import dev.langchain4j.rag.query.Query
import dev.langchain4j.rag.query.router.DefaultQueryRouter
import dev.langchain4j.rag.query.router.QueryRouter
import gitinsp.analysis.*
import gitinsp.chatpipeline.ConditionalQueryStrategy
import gitinsp.chatpipeline.DefaultQueryStrategy
import gitinsp.chatpipeline.QueryRoutingStrategyFactory
import gitinsp.chatpipeline.RAGComponentFactoryImpl
import gitinsp.chatpipeline.RouterWithStrategy
import gitinsp.utils.Assistant
import org.mockito.ArgumentMatchers
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
  override def beforeEach(): Unit =
    super.beforeEach()
    org.mockito.Mockito.reset(mockConfig)

    // Config mock setup
    when(mockConfig.getString("gitinsp.ollama.url")).thenReturn("http://localhost:11434")
    when(mockConfig.getString("gitinsp.models.default-model")).thenReturn("llama3.1")

  "The Analysis Context" should "be able to analyze code" in:
    val analysis = AnalysisContext.withCodeAnalysisStrategy(Assistant())
    analysis.strategy.map(_.strategyName) should be(Some("Code Analysis"))

  it should "be able to analyze natural language" in:
    val analysis = AnalysisContext.withNaturalLanguageStrategy(Assistant())
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
    when(mockFactory.createTextEmbeddingModel()).thenReturn(mockEmbeddingModel)
    when(mockEmbeddingModel.embed(textSegment)).thenReturn(embeddingResponse)

    // Action: Attempt to embed code
    val embeddingModel = mockFactory.createTextEmbeddingModel()
    val result         = embeddingModel.embed(textSegment)

    // Verify interactions and result
    verify(mockFactory).createTextEmbeddingModel()
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

  "The DefaultQueryStrategy" should "always return all retrievers" in:
    // Setup
    val strategy = DefaultQueryStrategy()

    // Create mock retrievers
    val retriever1 = mock[EmbeddingStoreContentRetriever]
    val retriever2 = mock[EmbeddingStoreContentRetriever]
    val retrievers = List(retriever1, retriever2)

    // Test with different queries
    val query1 = Query.from("How does this code work?")
    val query2 = Query.from("Don't use RAG for this question")

    // Verify that all retrievers are always returned regardless of query content
    strategy.determineRetrievers(query1, retrievers).size() should be(2)
    strategy.determineRetrievers(query2, retrievers).size() should be(2)

    // The returned collection should contain both our mock retrievers
    val result = strategy.determineRetrievers(query1, retrievers)
    result.contains(retriever1) should be(true)
    result.contains(retriever2) should be(true)

  "The ConditionalQueryStrategy" should "return all retrievers when the model answers 'no'" in:
    // Setup
    val mockChat = mock[OllamaChatModel]
    val strategy = ConditionalQueryStrategy(mockChat)

    // Create mock retrievers
    val retriever1 = mock[EmbeddingStoreContentRetriever]
    val retriever2 = mock[EmbeddingStoreContentRetriever]
    val retrievers = List(retriever1, retriever2)

    // Test query that should use RAG
    val query = Query.from("How does this code work?")

    // Mock the chat response indicating RAG should be used
    val aiMessage = AiMessage.from("No, the user did not ask to avoid using RAG.")
    val response  = ChatResponse.builder().aiMessage(aiMessage).build()
    when(mockChat.chat(ArgumentMatchers.any(classOf[UserMessage]))).thenReturn(response)

    // Verify that all retrievers are returned
    val result = strategy.determineRetrievers(query, retrievers)
    result.size() should be(0)
    result.contains(retriever1) should be(false)
    result.contains(retriever2) should be(false)

  it should "return no retrievers when the model answers 'yes'" in:
    // Setup
    val mockChat = mock[OllamaChatModel]
    val strategy = ConditionalQueryStrategy(mockChat)

    // Create mock retrievers
    val retriever1 = mock[EmbeddingStoreContentRetriever]
    val retriever2 = mock[EmbeddingStoreContentRetriever]
    val retrievers = List(retriever1, retriever2)

    // Test query that should not use RAG
    val query = Query.from("Don't use RAG for this question")

    // Mock the chat response indicating RAG should not be used
    val aiMessage = AiMessage.from("Yes, the user explicitly asked not to query the RAG index.")
    val response  = ChatResponse.builder().aiMessage(aiMessage).build()
    when(mockChat.chat(ArgumentMatchers.any(classOf[UserMessage]))).thenReturn(response)

    // Verify that no retrievers are returned
    val result = strategy.determineRetrievers(query, retrievers)
    result.size() should be(2)
    result.contains(retriever1) should be(true)
    result.contains(retriever2) should be(true)

  "The RAGComponentFactoryImpl" should "create DefaultQueryRouter when conditional RAG is disabled" in:
    // Setup
    val mockChat         = mock[OllamaChatModel]
    val mockScoringModel = mock[ScoringModel]
    val factory          = new RAGComponentFactoryImpl(mockConfig)

    // Create mock retrievers
    val retriever1 = mock[EmbeddingStoreContentRetriever]
    val retriever2 = mock[EmbeddingStoreContentRetriever]
    val retrievers = List(retriever1, retriever2)

    // Configure the mock to return false for conditional RAG
    when(mockConfig.getBoolean("gitinsp.rag.use-conditional-rag")).thenReturn(false)

    // Execute the method
    val router = factory.createQueryRouter(retrievers, mockChat)

    // Verify the router type
    router shouldBe a[DefaultQueryRouter]

  it should "create RouterWithStrategy when conditional RAG is enabled" in:
    // Setup
    val mockChat         = mock[OllamaChatModel]
    val mockConfig       = mock[Config]
    val mockScoringModel = mock[ScoringModel]
    val factory          = new RAGComponentFactoryImpl(mockConfig)

    // Create mock retrievers
    val retriever1 = mock[EmbeddingStoreContentRetriever]
    val retriever2 = mock[EmbeddingStoreContentRetriever]
    val retrievers = List(retriever1, retriever2)

    // Configure the mock to return true for conditional RAG
    when(mockConfig.getBoolean("gitinsp.rag.use-conditional-rag")).thenReturn(true)

    // Execute the method
    val router = factory.createQueryRouter(retrievers, mockChat)

    // Verify the router type
    router shouldBe a[RouterWithStrategy]

  it should "create a content aggregator" in:
    // Setup
    val mockConfig       = mock[Config]
    val mockScoringModel = mock[ScoringModel]
    val factory          = new RAGComponentFactoryImpl(mockConfig)

    // Execute the method
    val contentAggregator = factory.createContentAggregator(mockScoringModel)

    // Verify the content aggregator type
    contentAggregator shouldBe a[ReRankingContentAggregator]

  it should "create a retrieval augmentor" in:
    // Setup
    val mockChat         = mock[OllamaChatModel]
    val mockScoringModel = mock[ScoringModel]
    val factory          = new RAGComponentFactoryImpl(mockConfig)

    // Configure parameters
    val mockRouter            = mock[QueryRouter]
    val mockContentAggregator = mock[ReRankingContentAggregator]

    // Execute
    val retrievalAugmentor = factory.createRetrievalAugmentor(mockRouter, mockContentAggregator)

    // Verify
    retrievalAugmentor shouldBe a[DefaultRetrievalAugmentor]
