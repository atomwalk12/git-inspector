package gitinsp.chatpipeline

import dev.langchain4j.model.ollama.OllamaEmbeddingModel
import dev.langchain4j.model.ollama.OllamaStreamingChatModel
import gitinsp.utils.Language

/** Factory trait for creating components of the Retrieval Augmented Generation (RAG) pipeline.
  * This interface allows for different implementations and configurations of RAG components.
  */
trait RAGComponentFactory {

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
