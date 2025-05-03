package gitinsp.domain.interfaces.application
import gitinsp.domain.models.Assistant
import gitinsp.domain.models.StreamedResponse

/** Service for processing user messages and generating AI responses
  * Handles interaction with the underlying language model through Assistant implementations
  */
trait ChatService:
  /** Processes a user message with the specified AI assistant and returns a streamed response
    * @param message The user's message to be processed by the AI
    * @param aiService The AI assistant implementation to use for generating the response
    * @return A streaming response containing the AI-generated content
    */
  def chat(message: String, aiService: Assistant): StreamedResponse
