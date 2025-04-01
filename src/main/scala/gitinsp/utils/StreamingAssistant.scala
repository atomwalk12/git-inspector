package gitinsp.utils

import dev.langchain4j.service.TokenStream

trait StreamingAssistant {
  def chat(msg: String): TokenStream
}

object StreamingAssistant {
  def apply(): StreamingAssistant = new StreamingAssistantImpl()

  private class StreamingAssistantImpl extends StreamingAssistant {
    @SuppressWarnings(Array("org.wartremover.warts.Null")) // TODO: Implement
    override def chat(msg: String): TokenStream =
      null
  }
}
