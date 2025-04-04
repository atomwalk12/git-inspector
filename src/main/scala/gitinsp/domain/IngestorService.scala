package gitinsp.domain

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import dev.langchain4j.data.document.Document
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor
import gitinsp.infrastructure.CacheService
import gitinsp.infrastructure.strategies.IngestionStrategyFactory
import gitinsp.utils.GitRepository
import io.qdrant.client.grpc.Collections
import io.qdrant.client.grpc.Collections.Distance.Cosine

extension (ingestor: EmbeddingStoreIngestor)
  def ingest(repository: GitRepository): Unit =
    repository.docs.foreach(doc => doc.createLangchainDocument().fold(())(ingestor.ingest))

trait IngestorService:
  def ingest(repository: GitRepository): Unit

object IngestorService:
  def apply(cache: CacheService, config: Config): IngestorService =
    new IngestorServiceImpl(cache, config)

  def apply(): IngestorService =
    new IngestorServiceImpl(CacheService(), ConfigFactory.load())

  private class IngestorServiceImpl(cache: CacheService, config: Config) extends IngestorService:
    val qdrant = cache.qdrantClient

    override def ingest(repository: GitRepository): Unit =
      // Get all collections
      val collections = qdrant.listCollectionsAsync().get()

      // Create the collection if it doesn't exist
      repository match
        case GitRepository(url, languages, _) if !collections.contains(s"$url") =>
          createCollection(repository)
        case _ => ()

      // Create ingestor
      repository.indexNames.foreach {
        case index =>
          val strategy = IngestionStrategyFactory.createStrategy("default", index.language, config)
          val ingestor = cache.getIngestor(index, strategy)
          ingestor.ingest(repository)
      }

    private def createCollection(repository: GitRepository): Unit =
      qdrant.createCollectionAsync(
        repository.toString,
        Collections.VectorParams
          .newBuilder()
          .setDistance(Cosine)
          .setSize(config.getInt("gitinsp.qdrant.dimension"))
          .build(),
      )
