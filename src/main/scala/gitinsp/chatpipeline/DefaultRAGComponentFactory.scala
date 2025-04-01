package gitinsp.chatpipeline

import com.typesafe.config.Config
import dev.langchain4j.model.ollama.OllamaStreamingChatModel

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
}
