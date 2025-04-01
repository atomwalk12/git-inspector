package gitinsp.chatpipeline

import com.typesafe.config.Config
import dev.langchain4j.model.ollama.OllamaChatModel
import dev.langchain4j.model.ollama.OllamaEmbeddingModel
import dev.langchain4j.model.ollama.OllamaStreamingChatModel
import dev.langchain4j.model.scoring.ScoringModel
import dev.langchain4j.rag.DefaultRetrievalAugmentor
import dev.langchain4j.rag.content.aggregator.ReRankingContentAggregator
import dev.langchain4j.rag.content.retriever.ContentRetriever
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever
import dev.langchain4j.rag.query.router.DefaultQueryRouter
import dev.langchain4j.rag.query.router.QueryRouter
import gitinsp.utils.Language

import scala.jdk.CollectionConverters.*

/** Default implementation of RAGComponentFactory that creates components based on configuration.
  *
  * @param config The application configuration
  * @param modelRouter The chat model for routing queries
  * @param scoringModel The scoring model for ranking results
  */
class RAGComponentFactoryImpl(
  config: Config,
  modelRouter: OllamaChatModel,
  scoringModel: ScoringModel,
) extends RAGComponentFactory:

  /** Creates a content aggregator using the reranker configuration.
    *
    * @return A ReRankingContentAggregator
    */
  override def createContentAggregator(): ReRankingContentAggregator =
    ReRankingContentAggregator
      .builder()
      .scoringModel(scoringModel)
      .maxResults(config.getInt("tinygpt.reranker.max-results"))
      .minScore(config.getDouble("tinygpt.reranker.min-score"))
      .build()

  /** Creates a query router based on configuration and available routing strategies.
    *
    * @param retrievers The list of content retrievers
    * @return A QueryRouter
    */
  override def createQueryRouter(retrievers: List[EmbeddingStoreContentRetriever]): QueryRouter =
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
  override def createEmbeddingModel(language: Language): OllamaEmbeddingModel =
    language match {
      case Language.MARKDOWN =>
        OllamaEmbeddingModel
          .builder()
          .baseUrl(config.getString("gitinsp.ollama.url"))
          .modelName(config.getString("gitinsp.text-embedding.model"))
          .build()
      case _ =>
        OllamaEmbeddingModel
          .builder()
          .baseUrl(config.getString("gitinsp.ollama.url"))
          .modelName(config.getString("gitinsp.code-embedding.model"))
          .build()
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
