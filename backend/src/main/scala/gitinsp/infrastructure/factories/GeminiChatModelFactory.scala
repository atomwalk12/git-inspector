package gitinsp.infrastructure.factories

import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import dev.langchain4j.model.chat.ChatLanguageModel
import dev.langchain4j.model.chat.StreamingChatLanguageModel
import dev.langchain4j.model.vertexai.VertexAiGeminiChatModel
import dev.langchain4j.model.vertexai.VertexAiGeminiStreamingChatModel
import gitinsp.domain.interfaces.infrastructure.ChatModelFactory

object GeminiChatModelFactory:
  def apply(config: Config): ChatModelFactory =
    GeminiChatModelFactoryImpl(config)

/** Gemini implementation of ChatModelFactory.
  * This factory creates chat models that connect to an Claude server.
  *
  * @param config The application configuration
  */
class GeminiChatModelFactoryImpl(config: Config) extends ChatModelFactory with LazyLogging:

  private val _modelName = config.getString("gitinsp.gemini.model")
  private val _project   = config.getString("gitinsp.gemini.project")
  private val _location  = config.getString("gitinsp.gemini.location")

  // Renamed methods to follow factory pattern naming convention
  def createModelName(): String = _modelName
  def createProject(): String   = _project
  def createLocation(): String  = _location

  /** Creates a standard (non-streaming) chat model.
    *
    * @return A GeminiChatModel
    */
  override def createChatModel(): ChatLanguageModel =
    logger.debug(s"Creating chat model with model=${createModelName()}, project=${createProject()}")
    VertexAiGeminiChatModel.builder()
      .project(createProject())
      .location(createLocation())
      .modelName(createModelName())
      .build();

  /** Creates a streaming chat model.
    *
    * @return A GeminiStreamingChatModel
    */
  override def createStreamingChatModel(): StreamingChatLanguageModel =
    logger.debug(s"Streaming model with model=${createModelName()}, project=${createProject()}")
    VertexAiGeminiStreamingChatModel.builder()
      .project(createProject())
      .location(createLocation())
      .modelName(createModelName())
      .build()
