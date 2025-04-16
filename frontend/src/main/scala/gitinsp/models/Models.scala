package gitinsp.models

case class StreamResponse(text: String)

case class FetchContentRequest(url: String, format: String, extension: String)
case class FetchIndexesResponse(indexes: Seq[String])

case class GenerateIndex(indexName: String, extensions: String)
case class RemoveIndex(indexName: String)

case class GenerateIndexResponse(result: String, indexName: String)
case class RemoveIndexResponse(result: String)
sealed trait IndexEvent
case class IndexGenerated(indexName: String)       extends IndexEvent
case object RefreshIndicesRequested                extends IndexEvent
case class RemoveIndexRequested(indexName: String) extends IndexEvent

case class ChatMessage(id: String, isBot: Boolean, content: String)

case class IndexOption(id: String, label: String)

object IndexOption:
  val default = new IndexOption("default", "None")
