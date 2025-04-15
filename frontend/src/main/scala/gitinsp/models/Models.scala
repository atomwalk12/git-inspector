package gitinsp.models

case class StreamResponse(text: String)

sealed trait IndexEvent
case class IndexGenerated(indexName: String) extends IndexEvent
case object RefreshIndicesRequested          extends IndexEvent

case class ChatMessage(id: String, isBot: Boolean, content: String)
