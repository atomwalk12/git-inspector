package gitinsp.domain.interfaces.infrastructure
import dev.langchain4j.model.chat.StreamingChatLanguageModel
import dev.langchain4j.model.ollama.OllamaChatModel
import dev.langchain4j.model.ollama.OllamaEmbeddingModel
import dev.langchain4j.model.scoring.ScoringModel
import dev.langchain4j.rag.DefaultRetrievalAugmentor
import dev.langchain4j.rag.RetrievalAugmentor
import dev.langchain4j.rag.content.aggregator.ReRankingContentAggregator
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever
import dev.langchain4j.rag.query.router.QueryRouter
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor
import dev.langchain4j.store.embedding.qdrant.QdrantEmbeddingStore
import gitinsp.domain.models.Assistant
import gitinsp.domain.models.Language
import io.qdrant.client.QdrantClient
import io.qdrant.client.grpc.Collections
import io.qdrant.client.grpc.Collections.Distance

import scala.util.Try

/** Factory for creating components of the Retrieval Augmented Generation (RAG) pipeline
  * Provides methods to instantiate and configure all necessary elements for vector search,
  * document ingestion, query processing, and AI-assisted retrieval
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
    * @param modelRouter The LLM router used for dynamic filtering
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
    * @param modelRouter The LLM model used for query routing decisions
    * @return A configured QueryRouter
    */
  def createQueryRouter(
    retrievers: List[EmbeddingStoreContentRetriever],
    modelRouter: OllamaChatModel,
  ): QueryRouter

  /** Creates a content aggregator for ranking and filtering retrieved content.
    * This allows to rerank results, potentially yielding more relevant content.
    *
    * @param scoringModel The scoring model used to rank retrieved content
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
    * @return A streaming chat model implementation
    */
  def createStreamingChatModel(): StreamingChatLanguageModel

  /** Creates a model router for routing queries
    *
    * @return An OllamaChatModel configured for query routing
    */
  def createModelRouter(): OllamaChatModel

  /** Creates a StreamingAssistant. This service is the main entry point for the RAG pipeline.
    *
    * @param model The chat model
    * @param augmentor The retrieval augmentor
    * @return A StreamingAssistant
    */
  def createAssistant(
    model: StreamingChatLanguageModel,
    augmentor: Option[RetrievalAugmentor],
  ): Assistant

  /** Creates an embedding store.
    *
    * @param client The Qdrant client to use
    * @param name The name of the collection to store embeddings
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

  /** Creates an ingestor for adding document embeddings to the vector database
    *
    * @param language The programming language of the documents to ingest
    * @param embeddingModel The embedding model to use for vectorizing documents
    * @param embeddingStore The vector store where embeddings will be saved
    * @param strategy The strategy defining how documents are processed and split
    * @return A configured EmbeddingStoreIngestor for the specified parameters
    */
  def createIngestor(
    language: Language,
    embeddingModel: OllamaEmbeddingModel,
    embeddingStore: QdrantEmbeddingStore,
    strategy: IngestionStrategy,
  ): EmbeddingStoreIngestor

  /** Creates a collection in Qdrant.
    *
    * @param name The name of the collection
    * @param client The Qdrant client to use
    * @param distance The distance metric to use for vector similarity
    * @return A Try indicating success or failure of the operation
    */
  def createCollection(name: String, client: QdrantClient, distance: Distance): Try[Unit]
