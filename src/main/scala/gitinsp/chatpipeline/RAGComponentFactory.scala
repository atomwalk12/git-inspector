package gitinsp.chatpipeline

import dev.langchain4j.model.ollama.OllamaEmbeddingModel
import dev.langchain4j.model.ollama.OllamaStreamingChatModel
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever
import dev.langchain4j.rag.query.router.QueryRouter
import gitinsp.utils.Language

/** Factory trait for creating components of the Retrieval Augmented Generation (RAG) pipeline.
  * This interface allows for different implementations and configurations of RAG components.
  */
trait RAGComponentFactory {

  /** Creates a QueryRouter based on the provided retrievers
    *
    * @param retrievers List of content retrievers to use
    * @return A configured QueryRouter
    */
  def createQueryRouter(retrievers: List[EmbeddingStoreContentRetriever]): QueryRouter

  /** Creates an embedding model for the given language.
    *
    * @param language The language
    * @return An OllamaEmbeddingModel
    */
  def createEmbeddingModel(language: Language): OllamaEmbeddingModel

  /** Creates a streaming chat model.
    *
    * @return An OllamaStreamingChatModel
    */
  def createStreamingChatModel(): OllamaStreamingChatModel
}
