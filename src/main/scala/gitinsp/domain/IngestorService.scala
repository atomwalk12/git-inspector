package gitinsp.domain

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import gitinsp.infrastructure.CacheService
import gitinsp.infrastructure.strategies.IngestionStrategyFactory
import gitinsp.utils.Given.given_Conversion_QdrantURL_String
import gitinsp.utils.IngestorServiceExtensions.ingest
import gitinsp.utils.QdrantURL
import gitinsp.utils.RepositoryWithLanguages
import io.qdrant.client.grpc.Collections
import io.qdrant.client.grpc.Collections.Distance.Cosine

import scala.util.Try

trait IngestorService:
  def ingest(repository: RepositoryWithLanguages): Unit
  def deleteRepository(repository: RepositoryWithLanguages): Unit
  def listCollections(): Try[List[String]]

object IngestorService:
  def apply(cache: CacheService, config: Config): IngestorService =
    new IngestorServiceImpl(cache, config)

  def apply(): IngestorService =
    new IngestorServiceImpl(CacheService(), ConfigFactory.load())

  private class IngestorServiceImpl(cache: CacheService, config: Config) extends IngestorService:
    // Fields
    val client = cache.qdrantClient

    override def ingest(repository: RepositoryWithLanguages): Unit =
      // Get all collections
      val collections = listCollections().getOrElse(List.empty).map(QdrantURL(_))

      // Create the collection if it doesn't exist
      repository.indexNames
        .filterNot(index => collections.contains(index))
        .foreach(index => cache.createCollection(index, Cosine))

      // Create ingestor and store documents
      repository.languages.zip(repository.indexNames).foreach {
        case (language, index) =>
          val strategy = IngestionStrategyFactory.createStrategy("default", language, config)
          val ingestor = cache.getIngestor(index, language, strategy)
          ingestor.ingest(repository, language)
      }

    override def deleteRepository(repository: RepositoryWithLanguages): Unit =
      // Get all collections
      val collections = listCollections().getOrElse(List.empty).map(QdrantURL(_))

      // Delete the collection if it exists
      repository.indexNames
        .filter(index => collections.contains(index))
        .foreach(index => cache.delete(index))

    override def listCollections(): Try[List[String]] =
      cache.listCollections()
