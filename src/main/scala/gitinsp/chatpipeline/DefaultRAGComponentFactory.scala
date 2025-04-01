package gitinsp.chatpipeline

import com.typesafe.config.Config
import dev.langchain4j.model.ollama.OllamaEmbeddingModel
import dev.langchain4j.model.ollama.OllamaStreamingChatModel
import gitinsp.utils.Language

/** Default implementation of RAGComponentFactory that creates components based on configuration.
  *
  * @param config The application configuration
  * @param modelRouter The chat model for routing queries
  * @param scoringModel The scoring model for ranking results
  */
class DefaultRAGComponentFactory(
  config: Config,
) extends RAGComponentFactory {

  /** Creates a streaming chat model.
    *
    * @return An OllamaStreamingChatModel
    */
  override def createStreamingChatModel(): OllamaStreamingChatModel = {
    OllamaStreamingChatModel
      .builder()
      .baseUrl(config.getString("tinygpt.ollama.url"))
      .modelName(config.getString("tinygpt.models.default-model"))
      .build()
  }

  /** Creates an embedding model optimized for the specified language.
    *
    * @param language The language to optimize for
    * @return An OllamaEmbeddingModel
    */
  override def createEmbeddingModel(language: Language): OllamaEmbeddingModel = {
    language match {
      case Language.MARKDOWN =>
        OllamaEmbeddingModel
          .builder()
          .baseUrl(config.getString("tinygpt.ollama.url"))
          .modelName(config.getString("tinygpt.text-embedding.model"))
          .build()
      case _ =>
        OllamaEmbeddingModel
          .builder()
          .baseUrl(config.getString("tinygpt.ollama.url"))
          .modelName(config.getString("tinygpt.code-embedding.model"))
          .build()
    }
  }
}
