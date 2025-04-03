package gitinsp.chatpipeline

import ai.onnxruntime.OrtSession
import com.typesafe.config.Config
import dev.langchain4j.memory.chat.MessageWindowChatMemory
import dev.langchain4j.model.ollama.OllamaChatModel
import dev.langchain4j.model.ollama.OllamaEmbeddingModel
import dev.langchain4j.model.ollama.OllamaStreamingChatModel
import dev.langchain4j.model.scoring.ScoringModel
import dev.langchain4j.model.scoring.onnx.OnnxScoringModel
import dev.langchain4j.rag.DefaultRetrievalAugmentor
import dev.langchain4j.rag.RetrievalAugmentor
import dev.langchain4j.rag.content.aggregator.ReRankingContentAggregator
import dev.langchain4j.rag.content.retriever.ContentRetriever
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever
import dev.langchain4j.rag.query.Query
import dev.langchain4j.rag.query.router.DefaultQueryRouter
import dev.langchain4j.rag.query.router.QueryRouter
import dev.langchain4j.service.AiServices
import dev.langchain4j.store.embedding.filter.Filter
import dev.langchain4j.store.embedding.qdrant.QdrantEmbeddingStore
import gitinsp.infrastructure.ContentFormatter
import gitinsp.utils.Assistant
import io.qdrant.client.QdrantClient
import io.qdrant.client.QdrantGrpcClient

import scala.jdk.CollectionConverters.*

object RAGComponentFactory:
  def apply(config: Config): RAGComponentFactory =
    RAGComponentFactoryImpl(config)

/** Default implementation of RAGComponentFactory that creates components based on configuration.
  *
  * @param config The application configuration
  */
class RAGComponentFactoryImpl(
  config: Config,
) extends RAGComponentFactory:

  /** Creates a retriever for the specified index.
    *
    * @param embeddingStore The embedding store
    * @param embeddingModel The embedding model
    * @param indexName The specific collection to be used
    * @return A retriever for the specified index
    */
  override def createMarkdownRetriever(
    embeddingStore: QdrantEmbeddingStore,
    embeddingModel: OllamaEmbeddingModel,
    indexName: String,
  ): EmbeddingStoreContentRetriever =
    EmbeddingStoreContentRetriever
      .builder()
      .embeddingStore(embeddingStore)
      .displayName(indexName)
      .maxResults(config.getInt("gitinsp.text-embedding.max-results"))
      .minScore(config.getDouble("gitinsp.text-embedding.min-score"))
      .embeddingModel(embeddingModel)
      .build()

  override def createCodeRetriever(
    embeddingStore: QdrantEmbeddingStore,
    embeddingModel: OllamaEmbeddingModel,
    indexName: String,
    modelRouter: OllamaChatModel,
  ): EmbeddingStoreContentRetriever =
    EmbeddingStoreContentRetriever
      .builder()
      .embeddingStore(embeddingStore)
      .dynamicFilter(query => ContentFormatter.applyDynamicFilter(query, config, modelRouter))
      .displayName(indexName)
      .maxResults(config.getInt("gitinsp.code-embedding.max-results"))
      .minScore(config.getDouble("gitinsp.code-embedding.min-score"))
      .embeddingModel(embeddingModel)
      .build()

  /** Creates a content aggregator using the reranker configuration.
    *
    * @return A ReRankingContentAggregator
    */
  override def createModelRouter(): OllamaChatModel =
    OllamaChatModel
      .builder()
      .baseUrl(config.getString("gitinsp.ollama.url"))
      .modelName(config.getString("gitinsp.rag.model"))
      .build();

  override def createContentAggregator(scoringModel: ScoringModel): ReRankingContentAggregator =
    ReRankingContentAggregator
      .builder()
      .scoringModel(scoringModel)
      .maxResults(config.getInt("gitinsp.reranker.max-results"))
      .minScore(config.getDouble("gitinsp.reranker.min-score"))
      .build()

  /** Creates a query router based on configuration and available routing strategies.
    *
    * @param retrievers The list of content retrievers
    * @return A QueryRouter
    */
  override def createQueryRouter(
    retrievers: List[EmbeddingStoreContentRetriever],
    modelRouter: OllamaChatModel,
  ): QueryRouter =
    if config.getBoolean("gitinsp.rag.use-conditional-rag") then
      // Use a strategy that conditionally routes based on the query
      val strategy = QueryRoutingStrategyFactory.createStrategy("conditional", modelRouter)

      // Create a custom router that uses the strategy
      // Basically this allows to query separately both markdown and code retrievers
      new RouterWithStrategy(strategy, retrievers)
    else
      // Use the default router. This means that all retrievers are used without relying on
      // an external LLM.
      new DefaultQueryRouter(retrievers.asJava)

  /** Creates a retrieval augmentor that combines the router and aggregator.
    *
    * @param router The query router
    * @param aggregator The content aggregator
    * @return A DefaultRetrievalAugmentor
    */
  override def createRetrievalAugmentor(
    router: QueryRouter,
    aggregator: ReRankingContentAggregator,
  ): DefaultRetrievalAugmentor =
    DefaultRetrievalAugmentor
      .builder()
      .queryRouter(router)
      .contentAggregator(aggregator)
      .build()

  /** Creates a streaming chat model.
    *
    * @return An OllamaStreamingChatModel
    */
  override def createStreamingChatModel(): OllamaStreamingChatModel =
    OllamaStreamingChatModel
      .builder()
      .baseUrl(config.getString("gitinsp.ollama.url"))
      .modelName(config.getString("gitinsp.models.default-model"))
      .build()

  /** Creates an embedding model optimized for the specified language.
    *
    * @param language The language to optimize for
    * @return An OllamaEmbeddingModel
    */
  override def createTextEmbeddingModel(): OllamaEmbeddingModel =
    OllamaEmbeddingModel
      .builder()
      .baseUrl(config.getString("gitinsp.ollama.url"))
      .modelName(config.getString("gitinsp.text-embedding.model"))
      .build()

  /** Creates an embedding model optimized for code.
    *
    * @return An OllamaEmbeddingModel
    */
  override def createCodeEmbeddingModel(): OllamaEmbeddingModel =
    OllamaEmbeddingModel
      .builder()
      .baseUrl(config.getString("gitinsp.ollama.url"))
      .modelName(config.getString("gitinsp.code-embedding.model"))
      .build()

  /** Creates an assistant. It is used to chat with the model.
    *
    * @param chatModel The chat model
    * @param retrievalAugmentor The retrieval augmentor
    * @return A streaming assistant
    */
  override def createAssistant(
    chatModel: OllamaStreamingChatModel,
    retrievalAugmentor: RetrievalAugmentor,
  ): Assistant =
    AiServices
      .builder(classOf[Assistant])
      .streamingChatLanguageModel(chatModel)
      .retrievalAugmentor(retrievalAugmentor)
      .chatMemory(MessageWindowChatMemory.withMaxMessages(30))
      .build()

  /** Creates an embedding store. It is used to store documents in a vector database.
    *
    * @param client The Qdrant client
    * @param name The name of the collection
    * @return A QdrantEmbeddingStore
    */
  override def createEmbeddingStore(client: QdrantClient, name: String): QdrantEmbeddingStore =
    QdrantEmbeddingStore
      .builder()
      .client(client)
      .host(config.getString("gitinsp.qdrant.host"))
      .port(config.getInt("gitinsp.qdrant.port"))
      .collectionName(name)
      .build()

  /** Creates a Qdrant client.
    *
    * @return A QdrantClient
    */
  override def createQdrantClient(): QdrantClient =
    new QdrantClient(
      QdrantGrpcClient
        .newBuilder(
          config.getString("gitinsp.qdrant.host"),
          config.getInt("gitinsp.qdrant.port"),
          false,
        )
        .build(),
    )

  /** Creates a scoring model. It is used to rerank documents (potentially yield better results).
    *
    * @return A ScoringModel
    */
  override def createScoringModel(): ScoringModel =
    val modelPath     = config.getString("gitinsp.reranker.model-path")
    val tokenizerPath = config.getString("gitinsp.reranker.tokenizer-path")
    val normalize     = config.getBoolean("gitinsp.reranker.normalize-scores")
    val max_length    = config.getInt("gitinsp.reranker.max-length")

    val options = new OrtSession.SessionOptions()
    config.getBoolean("gitinsp.reranker.use-gpu") match {
      case true =>
        options.addCUDA(0);
        options.addCPU(true);
        new OnnxScoringModel(modelPath, options, tokenizerPath, max_length, normalize)
      case false =>
        new OnnxScoringModel(modelPath, options, tokenizerPath, max_length, normalize)
    }

/** A custom router that uses a query routing strategy.
  * This is an adapter that bridges the gap between the QueryRouter interface and the
  * QueryRoutingStrategy.
  *
  * @param strategy The query routing strategy
  * @param retrievers The list of content retrievers
  */
class RouterWithStrategy(
  strategy: QueryRoutingStrategy,
  retrievers: List[EmbeddingStoreContentRetriever],
) extends QueryRouter:

  /** Routes the query to the appropriate retrievers based on the strategy.
    *
    * @param query The query to route
    * @return A collection of content retrievers
    */
  override def route(query: dev.langchain4j.rag.query.Query)
    : java.util.Collection[ContentRetriever] =
    strategy.determineRetrievers(query, retrievers)

/** Factory trait for creating components of the Retrieval Augmented Generation (RAG) pipeline.
  * This interface allows for different implementations and configurations of RAG components.
  */
trait RAGComponentFactory:

  /** Creates a specified retriever type. The retriever is used to embed documents using the
    * embedding model and store them in the embedding store.
    *
    * @param embeddingStore The embedding store
    * @param embeddingModel The embedding model
    * @param indexName The specific collection to be used
    * @return A retriever for the specified index
    */
  def createMarkdownRetriever(
    embeddingStore: QdrantEmbeddingStore,
    embeddingModel: OllamaEmbeddingModel,
    indexName: String,
  ): EmbeddingStoreContentRetriever

  /** Creates a retriever for code.
    *
    * @param embeddingStore The embedding store
    * @param embeddingModel The embedding model
    * @param indexName The specific collection to be used
    * @param dynamicFilter A function that returns a filter for the query. It can be used to filter the documents based on a specific language
    * @return A retriever for the specified index
    */
  def createCodeRetriever(
    embeddingStore: QdrantEmbeddingStore,
    embeddingModel: OllamaEmbeddingModel,
    indexName: String,
    modelRouter: OllamaChatModel,
  ): EmbeddingStoreContentRetriever

  /** Creates a QueryRouter based on the provided retrievers
    *
    * @param retrievers List of content retrievers to use
    * @return A configured QueryRouter
    */
  def createQueryRouter(
    retrievers: List[EmbeddingStoreContentRetriever],
    modelRouter: OllamaChatModel,
  ): QueryRouter

  /** Creates a content aggregator for ranking and filtering retrieved content.
    * This allows to rerank results, potentially yielding more relevant content.
    *
    * @return A configured ReRankingContentAggregator
    */
  def createContentAggregator(scoringModel: ScoringModel): ReRankingContentAggregator

  /** Creates a RetrievalAugmentor that combines router and aggregator
    *
    * @param router The QueryRouter to use
    * @param aggregator The ContentAggregator to use
    * @return A configured DefaultRetrievalAugmentor
    */
  def createRetrievalAugmentor(
    router: QueryRouter,
    aggregator: ReRankingContentAggregator,
  ): DefaultRetrievalAugmentor

  /** Creates an embedding model for text.
    *
    * @return An OllamaEmbeddingModel
    */
  def createTextEmbeddingModel(): OllamaEmbeddingModel

  /** Creates an embedding model for code.
    *
    * @return An OllamaEmbeddingModel
    */
  def createCodeEmbeddingModel(): OllamaEmbeddingModel

  /** Creates a streaming chat model.
    *
    * @return An OllamaStreamingChatModel
    */
  def createStreamingChatModel(): OllamaStreamingChatModel

  /** Create model router
    *
    * @return An OllamaChatModel
    */
  def createModelRouter(): OllamaChatModel

  /** Creates a StreamingAssistant. This service is the main entry point for the RAG pipeline.
    *
    * @param model The chat model
    * @param augmentor The retrieval augmentor
    * @return A StreamingAssistant
    */
  def createAssistant(model: OllamaStreamingChatModel, augmentor: RetrievalAugmentor): Assistant

  /** Creates an embedding store.
    *
    * @return A QdrantEmbeddingStore
    */
  def createEmbeddingStore(client: QdrantClient, name: String): QdrantEmbeddingStore

  /** Creates a Qdrant client.
    *
    * @return A QdrantClient
    */
  def createQdrantClient(): QdrantClient

  /** Creates a scoring model. It is used to rerank documents (potentially yield better results).
    *
    * @return A ScoringModel
    */
  def createScoringModel(): ScoringModel
