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

  /** Entry point for splitting a single Langchain4j Document. */
  def split(doc: Document): java.util.List[TextSegment] = {
    // createDocuments(doc)
    createDocuments(List(doc.text()), Some(List(doc.metadata())))
  }

  /** Abstract method to be implemented by subclasses. Defines the core splitting logic for a
    * given text.
    */
  def splitText(text: String): List[String]

  /** Creates TextSegment objects from a list of texts and optional metadata. It calls the
    * subclass's splitText implementation to get initial chunks and then formats them into
    * TextSegments, potentially adding start index metadata.
    */
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

      // Calls the specific splitText implementation (e.g., RecursiveCharacterTextSplitter)
      for chunk <- splitText(texts(i)) do {
        val metadata = _metadatas(i)
        // Optionally add the start index of the chunk within the original text
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

    // Convert internal Document representations to TextSegments for the final output
    documents.map(doc => createSegment(doc.text, doc, index.getAndIncrement())).toList.asJava
  }

  def createSegment(text: String, document: Document, index: Int): TextSegment = {
    val metadata = document.metadata.copy().put("index", String.valueOf(index))
    TextSegment.from(text, metadata)
  }

  /** Splits multiple documents into a list of TextSegments. */
  def splitDocuments(documents: Iterable[Document]): List[TextSegment] = {
    val texts     = ListBuffer[String]()
    val metadatas = ListBuffer[Metadata]()

    for doc <- documents do {
      texts += doc.text
      metadatas += doc.metadata
    }

    createDocuments(texts.toList, Some(metadatas.toList)).asScala.toList
  }

  /** Helper to join a list of strings with a separator, optionally trimming whitespace. */
  protected def joinDocs(docs: List[String], separator: String): Option[String] = {
    val text          = docs.mkString(separator)
    val processedText = if stripWhitespace then text.trim else text

    if processedText.isEmpty then None else Some(processedText)
  }

  /** Merges smaller text splits into larger chunks that respect the `chunkSize`. It attempts to
    * maximize chunk size without exceeding it, while also considering `chunkOverlap`.
    */
  protected def mergeSplits(splits: Iterable[String], separator: String): List[String] = {
    val separatorLen = lengthFunction(separator)

    val docs       = ListBuffer[String]() // Final merged chunks
    var currentDoc = ListBuffer[String]() // Current chunk being built
    var total      = 0                    // Current length of the chunk being built

    for d <- splits do {
      val len = lengthFunction(d)
      // Check if adding the next split exceeds the chunkSize
      if total + len + (if currentDoc.nonEmpty then separatorLen else 0) > chunkSize then {
        if total > chunkSize then {
          logger.warn(
            s"Created a chunk of size $total, " +
              s"which is longer than the specified $chunkSize",
          )
        }

        // Finalize the current chunk if it's not empty
        if currentDoc.nonEmpty then {
          val doc = joinDocs(currentDoc.toList, separator)
          if doc.isDefined then {
            docs += doc.get
          }
          // Overlap Handling: Remove splits from the beginning of `currentDoc`
          // until the total length allows adding the new split `d` without exceeding `chunkSize`,
          // OR until the remaining length is less than `chunkOverlap`.
          // This ensures continuity between the finalized chunk and the next one being started.
          while total > chunkOverlap ||
            (total + len + (if currentDoc.nonEmpty then separatorLen
                            else 0) > chunkSize && total > 0)
          do {
            total -= lengthFunction(currentDoc.head) + (if currentDoc.length > 1 then separatorLen
                                                        else 0)
            currentDoc = currentDoc.tail // Remove the first element
          }
        }
      }

      // Add the current split to the chunk being built
      currentDoc += d
      total += len + (if currentDoc.length > 1 then separatorLen else 0)
    }

    // Add the last remaining chunk after the loop finishes
    val doc = joinDocs(currentDoc.toList, separator)
    if doc.isDefined then {
      docs += doc.get
    }

    docs.toList
  }
}
