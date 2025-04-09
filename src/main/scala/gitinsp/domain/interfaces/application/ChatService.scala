package gitinsp.domain.interfaces.application
import gitinsp.domain.models.Assistant
import gitinsp.domain.models.StreamedResponse

trait ChatService:
  def chat(message: String, aiService: Assistant): StreamedResponse
