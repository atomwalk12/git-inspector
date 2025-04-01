package gitinsp.chatpipeline

import dev.langchain4j.model.ollama.OllamaStreamingChatModel

/** Factory trait for creating components of the Retrieval Augmented Generation (RAG) pipeline.
  * This interface allows for different implementations and configurations of RAG components.
  */
trait RAGComponentFactory {

  /** Creates a streaming chat model.
    *
    * @return An OllamaStreamingChatModel
    */
  def createStreamingChatModel(): OllamaStreamingChatModel
}
