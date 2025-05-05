package gitinsp.domain.interfaces.infrastructure

import dev.langchain4j.data.document.Document
import dev.langchain4j.data.document.DocumentSplitter
import dev.langchain4j.data.segment.TextSegment
import gitinsp.domain.models.Language

/** Strategy for preprocessing and preparing documents for vector database ingestion
  * Implements different approaches for transforming and splitting content based on content type
  */
trait IngestionStrategy:
  /** Transforms a text segment before storing it in the vector database
    * @param textSegment The text segment to transform
    * @return The transformed text segment
    */
  def textSegmentTransformer(textSegment: TextSegment): TextSegment

  /** Transforms a document before splitting it into segments
    * @param document The document to transform
    * @return The transformed document
    */
  def documentTransformer(document: Document): Document

  /** Creates a document splitter appropriate for the given language
    * @param lang The programming language of the document content
    * @param chunkSize The target size of each document chunk
    * @param overlap The number of tokens to overlap between chunks
    * @return A DocumentSplitter configured for the language
    */
  def documentSplitter(lang: Language, chunkSize: Int, overlap: Int): DocumentSplitter
