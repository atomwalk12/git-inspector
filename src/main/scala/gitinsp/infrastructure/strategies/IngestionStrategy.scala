package gitinsp.infrastructure.strategies

import com.typesafe.config.Config
import dev.langchain4j.data.document.Document
import dev.langchain4j.data.document.DocumentSplitter
import dev.langchain4j.data.segment.TextSegment
import gitinsp.domain.interfaces.infrastructure.IngestionStrategy
import gitinsp.domain.interfaces.infrastructure.IngestionStrategyFactory
import gitinsp.domain.models.Category
import gitinsp.domain.models.Language
import gitinsp.infrastructure.parser.RecursiveCharacterTextSplitter

object IngestionStrategy:
  def apply(lang: Language, config: Config): IngestionStrategy =
    new DefaultIngestionStrategy(lang, config)

  class DefaultIngestionStrategy(lang: Language, config: Config) extends IngestionStrategy:
    override def textSegmentTransformer(textSegment: TextSegment): TextSegment =
      TextSegment.from(
        "File name: " + textSegment.metadata().getString("file_name") + "\n" + textSegment
          .text(),
        textSegment.metadata(),
      )

    override def documentTransformer(document: Document): Document =
      document

    override def documentSplitter(lang: Language, chunkSize: Int, overlap: Int): DocumentSplitter =
      val codeChunkSize = config.getInt("gitinsp.code-embedding.chunk-size")
      val textChunkSize = config.getInt("gitinsp.text-embedding.chunk-size")
      val chunkSize     = if lang.category == Category.CODE then codeChunkSize else textChunkSize

      val codeChunkOverlap = config.getInt("gitinsp.code-embedding.chunk-overlap")
      val textChunkOverlap = config.getInt("gitinsp.text-embedding.chunk-overlap")
      val overlap = if lang.category == Category.CODE then codeChunkOverlap else textChunkOverlap

      // This splits the document into chunks of text. All code languages use the same parameters,
      // however these are configurable w.r.t. the text embeddings.
      RecursiveCharacterTextSplitter.fromLanguage(
        lang,
        chunkSize = chunkSize,
        chunkOverlap = overlap,
      )

object IngestionStrategyFactory extends IngestionStrategyFactory:
  def createStrategy(strategyType: String, lang: Language, config: Config): IngestionStrategy =
    strategyType.toLowerCase(java.util.Locale.ROOT) match
      case "default" => IngestionStrategy(lang, config)
      case _         => IngestionStrategy(lang, config)
