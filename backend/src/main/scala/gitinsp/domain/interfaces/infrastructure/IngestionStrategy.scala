package gitinsp.domain.interfaces.infrastructure

import dev.langchain4j.data.document.Document
import dev.langchain4j.data.document.DocumentSplitter
import dev.langchain4j.data.segment.TextSegment
import gitinsp.domain.models.Language

trait IngestionStrategy:
  def textSegmentTransformer(textSegment: TextSegment): TextSegment
  def documentTransformer(document: Document): Document
  def documentSplitter(lang: Language, chunkSize: Int, overlap: Int): DocumentSplitter
