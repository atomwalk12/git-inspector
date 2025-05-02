package gitinsp.infrastructure.factories

import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import dev.langchain4j.model.anthropic.AnthropicChatModel
import dev.langchain4j.model.anthropic.AnthropicStreamingChatModel
import dev.langchain4j.model.chat.ChatLanguageModel
import dev.langchain4j.model.chat.StreamingChatLanguageModel
import gitinsp.domain.interfaces.infrastructure.ChatModelFactory

object ClaudeFactoryProvider:
  def apply(config: Config): ChatModelFactory =
    ClaudeFactoryProviderImpl(config)

/** Claude implementation of ChatModelFactory.
  * This factory creates chat models that connect to an Claude server.
  *
  * @param config The application configuration
  */
class ClaudeFactoryProviderImpl(config: Config) extends ChatModelFactory with LazyLogging:

  private val _modelName       = config.getString("gitinsp.claude.model")
  private val _envVariable     = config.getString("gitinsp.claude.antrophic-api-key")
  private val _antrophicApiKey = System.getenv(_envVariable)

  // Renamed methods to follow factory pattern naming convention
  def createModelName(): String   = _modelName
  def createEnvVariable(): String = _envVariable
  def createApiKey(): String      = _antrophicApiKey

  /** Creates a standard (non-streaming) chat model.
    *
    * @return A ClaudeChatModel
    */
  override def createChatModel(): ChatLanguageModel =
    logger.debug(s"Creating chat model with model=${createModelName()}")
    AnthropicChatModel.builder()
      .apiKey(createApiKey())
      .modelName(createModelName())
      .logRequests(true)
      .logResponses(true)
      .build();

  /** Creates a streaming chat model.
    *
    * @return A ClaudeStreamingChatModel
    */
  override def createStreamingChatModel(): StreamingChatLanguageModel =
    logger.debug(s"Creating chat model with model=${createModelName()}")
    AnthropicStreamingChatModel.builder()
      .apiKey(createApiKey())
      .modelName(createModelName())
      .logRequests(true)
      .build();
