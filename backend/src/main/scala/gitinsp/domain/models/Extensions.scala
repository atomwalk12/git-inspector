package gitinsp.domain.models

import dev.langchain4j.store.embedding.EmbeddingStoreIngestor
object Given:

  given Conversion[QdrantURL, String] = url => url.value

  given Conversion[AIServiceURL, String] = url => url.value

  given Conversion[URL, String] = _.value

  given Conversion[URL, AIServiceURL] = url => url.toAIServiceURL()

object IngestorServiceExtensions:
  extension (ingestor: EmbeddingStoreIngestor)
    def ingest(repository: GitRepository, lang: Language): Unit =
      // Get all documents of the specified language
      repository.docs.filter(_.language == lang).foreach(
        doc => doc.createLangchainDocument().fold(())(ingestor.ingest),
      )
