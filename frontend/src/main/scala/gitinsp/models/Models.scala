package gitinsp.models

final case class StreamResponse(text: String)

final case class FetchContentRequest(url: String, format: String, extension: String)
final case class FetchIndexesResponse(indexes: Seq[String])

final case class GenerateIndex(indexName: String, extensions: String)
final case class RemoveIndex(indexName: String)

final case class GenerateIndexResponse(result: String, indexName: String)
final case class RemoveIndexResponse(result: String)
sealed trait IndexEvent
final case class IndexGenerated(indexName: String)       extends IndexEvent
case object RefreshIndicesRequested                      extends IndexEvent
final case class RemoveIndexRequested(indexName: String) extends IndexEvent

final case class ChatMessage(id: String, isBot: Boolean, content: String)

final case class IndexOption(id: String, label: String)

object IndexOption:
  val default = new IndexOption("default", "None")

type ChatSession = Seq[ChatMessage]
