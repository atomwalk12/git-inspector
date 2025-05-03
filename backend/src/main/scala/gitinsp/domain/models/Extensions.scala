package gitinsp.domain.models

import dev.langchain4j.store.embedding.EmbeddingStoreIngestor

/** Provides type conversions for various URL types */
object Given:
  /** Converts QdrantURL to String by extracting its value */
  given Conversion[QdrantURL, String] = url => url.value

  /** Converts AIServiceURL to String by extracting its value */
  given Conversion[AIServiceURL, String] = url => url.value

  /** Converts URL to String by extracting its value */
  given Conversion[URL, String] = _.value

  /** Converts URL to AIServiceURL */
  given Conversion[URL, AIServiceURL] = url => url.toAIServiceURL()

/** Provides extensions for the EmbeddingStoreIngestor class */
object IngestorServiceExtensions:
  extension (ingestor: EmbeddingStoreIngestor)
    /** Ingests documents from a Git repository filtered by language
      * @param repository The Git repository containing documents to ingest
      * @param lang The programming language to filter documents by
      * @return Unit
      */
    def ingest(repository: GitRepository, lang: Language): Unit =
      // Get all documents of the specified language
      repository.docs.filter(_.language == lang).foreach(
        doc => doc.createLangchainDocument().fold(())(ingestor.ingest),
      )
