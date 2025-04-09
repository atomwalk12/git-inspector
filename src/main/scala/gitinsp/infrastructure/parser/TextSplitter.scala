package gitinsp.infrastructure.parser

import com.typesafe.scalalogging.LazyLogging
import dev.langchain4j.data.document.Document
import dev.langchain4j.data.document.DocumentSplitter
import dev.langchain4j.data.document.Metadata
import dev.langchain4j.data.segment.TextSegment

import java.util.concurrent.atomic.AtomicInteger

import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters.*

@SuppressWarnings(Array(
  "org.wartremover.warts.DefaultArguments",
  "org.wartremover.warts.MutableDataStructures",
  "org.wartremover.warts.Var",
  "org.wartremover.warts.IterableOps",
  "org.wartremover.warts.SizeIs",
  "org.wartremover.warts.OptionPartial",
  "org.wartremover.warts.While",
  "org.wartremover.warts.SeqApply",
))
/** Interface for splitting text into chunks. */
abstract class TextSplitter(
  val chunkSize: Int = 4000,
  val chunkOverlap: Int = 200,
  val lengthFunction: String => Int = _.length,
  val keepSeparator: Either[Boolean, String] = Left(false), // Either[Boolean, "start" | "end"]
  val addStartIndex: Boolean = false,
  val stripWhitespace: Boolean = true,
) extends DocumentSplitter with LazyLogging {

  require(
    chunkOverlap <= chunkSize,
    s"Got a larger chunk overlap ($chunkOverlap) than chunk size ($chunkSize), should be smaller.",
  )

  def split(doc: Document): java.util.List[TextSegment] = {
    // createDocuments(doc)
    createDocuments(List(doc.text()), Some(List(doc.metadata())))
  }

  /** Split text into multiple components. */
  def splitText(text: String): List[String]

  /** Create documents from a list of texts. */
  def createDocuments(
    texts: List[String],
    metadatas: Option[List[Metadata]] = None,
  ): java.util.List[TextSegment] = {
    val _metadatas = metadatas.getOrElse(List.fill(texts.length)(new Metadata()))
    val index      = new AtomicInteger(0)
    val documents  = ListBuffer[Document]()

    for i <- texts.indices do {
      var index            = 0
      var previousChunkLen = 0

      for chunk <- splitText(texts(i)) do {
        val metadata = _metadatas(i)
        val updatedMetadata = if addStartIndex then {
          val offset = index + previousChunkLen - chunkOverlap
          index = texts(i).indexOf(chunk, math.max(0, offset))
          previousChunkLen = chunk.length
          metadata.put("start_index", String.valueOf(index))
        }
        else {
          metadata
        }

        val newDoc = Document.from(chunk, updatedMetadata)
        documents += newDoc
      }
    }

    documents.map(doc => createSegment(doc.text, doc, index.getAndIncrement())).toList.asJava
  }

  def createSegment(text: String, document: Document, index: Int): TextSegment = {
    val metadata = document.metadata.copy().put("index", String.valueOf(index))
    TextSegment.from(text, metadata)
  }

  /** Split documents. */
  def splitDocuments(documents: Iterable[Document]): List[TextSegment] = {
    val texts     = ListBuffer[String]()
    val metadatas = ListBuffer[Metadata]()

    for doc <- documents do {
      texts += doc.text
      metadatas += doc.metadata
    }

    createDocuments(texts.toList, Some(metadatas.toList)).asScala.toList
  }

  /** Join docs with a separator. */
  protected def joinDocs(docs: List[String], separator: String): Option[String] = {
    val text          = docs.mkString(separator)
    val processedText = if stripWhitespace then text.trim else text

    if processedText.isEmpty then None else Some(processedText)
  }

  /** Merge splits into chunks. */
  protected def mergeSplits(splits: Iterable[String], separator: String): List[String] = {
    // We now want to combine these smaller pieces into medium size
    // chunks to send to the LLM.
    val separatorLen = lengthFunction(separator)

    val docs       = ListBuffer[String]()
    var currentDoc = ListBuffer[String]()
    var total      = 0

    for d <- splits do {
      val len = lengthFunction(d)
      if total + len + (if currentDoc.nonEmpty then separatorLen else 0) > chunkSize then {
        if total > chunkSize then {
          logger.warn(
            s"Created a chunk of size $total, " +
              s"which is longer than the specified $chunkSize",
          )
        }

        if currentDoc.nonEmpty then {
          val doc = joinDocs(currentDoc.toList, separator)
          if doc.isDefined then {
            docs += doc.get
          }
          // Keep on popping if:
          // - we have a larger chunk than in the chunk overlap
          // - or if we still have any chunks and the length is long
          while total > chunkOverlap ||
            (total + len + (if currentDoc.nonEmpty then separatorLen
                            else 0) > chunkSize && total > 0)
          do {

            total -= lengthFunction(currentDoc.head) + (if currentDoc.length > 1 then separatorLen
                                                        else 0)
            currentDoc = currentDoc.tail
          }
        }
      }

      currentDoc += d
      total += len + (if currentDoc.length > 1 then separatorLen else 0)
    }

    val doc = joinDocs(currentDoc.toList, separator)
    if doc.isDefined then {
      docs += doc.get
    }

    docs.toList
  }
}
