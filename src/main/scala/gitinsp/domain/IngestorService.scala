package gitinsp.domain

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import dev.langchain4j.data.document.Document
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor
import gitinsp.infrastructure.CacheService
import gitinsp.infrastructure.strategies.IngestionStrategyFactory
import gitinsp.utils.GitRepository
import gitinsp.utils.IndexName
import gitinsp.utils.Language
import io.qdrant.client.QdrantClient
import io.qdrant.client.grpc.Collections
import io.qdrant.client.grpc.Collections.Distance.Cosine

import scala.jdk.CollectionConverters.ListHasAsScala

extension (ingestor: EmbeddingStoreIngestor)
  def ingest(repository: GitRepository, lang: Language): Unit =
    repository.docs.filter(_.language == lang).foreach(
      doc =>
        doc.createLangchainDocument()
          .fold(())(ingestor.ingest),
    )

extension (qdrantClient: QdrantClient)
  def delete(index: IndexName): Unit =
    qdrantClient.deleteCollectionAsync(index.name).get

  def listCollections(): List[String] =
    qdrantClient.listCollectionsAsync().get().asScala.toList

trait IngestorService:
  def ingest(repository: GitRepository): Unit
  def deleteRepository(repository: GitRepository): Unit

object IngestorService:
  def apply(cache: CacheService, config: Config): IngestorService =
    new IngestorServiceImpl(cache, config)

  def apply(): IngestorService =
    new IngestorServiceImpl(CacheService(), ConfigFactory.load())

  private class IngestorServiceImpl(cache: CacheService, config: Config) extends IngestorService:

    val client = cache.qdrantClient

    override def ingest(repository: GitRepository): Unit =
      // Get all collections
      val collections = client.listCollections()

      // Create the collection if it doesn't exist
      repository.indexNames
        .filterNot(index => collections.contains(index.name))
        .foreach(index => cache.createCollection(index.name, Cosine))

      // Create ingestor
      repository.indexNames.foreach {
        case index =>
          val strategy = IngestionStrategyFactory.createStrategy("default", index.language, config)
          val ingestor = cache.getIngestor(index, strategy)
          ingestor.ingest(repository, index.language)
      }

    override def deleteRepository(repository: GitRepository): Unit =
      val collections = client.listCollections()
      repository.indexNames
        .filter(index => collections.contains(index.name))
        .foreach(index => client.delete(index))
