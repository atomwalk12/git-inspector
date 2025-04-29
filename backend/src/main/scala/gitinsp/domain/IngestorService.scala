package gitinsp.domain

import com.typesafe.config.Config
import gitinsp.domain.interfaces.application.IngestorService
import gitinsp.domain.interfaces.infrastructure.CacheService
import gitinsp.domain.interfaces.infrastructure.IngestionStrategyFactory
import gitinsp.domain.models.GitRepository
import gitinsp.domain.models.Given.given_Conversion_QdrantURL_String
import gitinsp.domain.models.IngestorServiceExtensions.ingest
import gitinsp.domain.models.QdrantURL
import io.qdrant.client.grpc.Collections
import io.qdrant.client.grpc.Collections.Distance.Cosine

import scala.language.implicitConversions
import scala.util.Failure
import scala.util.Try

object IngestorService:
  def apply(
    cache: CacheService,
    config: Config,
    strategyFactory: IngestionStrategyFactory,
  ): IngestorService =
    new IngestorServiceImpl(cache, config, strategyFactory)

  private class IngestorServiceImpl(
    cache: CacheService,
    config: Config,
    strategyFactory: IngestionStrategyFactory,
  ) extends IngestorService:
    // Fields
    val client = cache.qdrantClient

    override def ingest(repository: GitRepository): Try[Unit] =
      // Get all collections
      val collections = listCollections().getOrElse(List.empty).map(QdrantURL(_))

      // Create the collection if it doesn't exist
      repository.indexNames
        .filterNot(index => collections.contains(index))
        .foreach(index => cache.createCollection(index, Cosine))

      // Create ingestor and store documents
      Try {
        repository.languages.zip(repository.indexNames).foreach {
          case (language, index) =>
            val strategy = strategyFactory.createStrategy("default", language, config)
            val ingestor = cache.getIngestor(index, language, strategy)
            ingestor.ingest(repository, language)
        }
      }.recoverWith {
        case e =>
          Failure(new Exception(s"Error ingesting repository: ${e.getMessage}"))
      }

    override def deleteRepository(repository: GitRepository): Try[Unit] =
      // Get all collections
      val collections = listCollections().getOrElse(List.empty).map(QdrantURL(_))

      // Delete the collection if it exists.
      // This is usually done when a repository is regenerated.
      Try {
        repository.indexNames
          .filter(index => collections.contains(index))
          .foreach(index => cache.delete(index))
      }.recoverWith {
        case e =>
          Failure(new Exception(s"Error deleting repository: ${e.getMessage}"))
      }

    override def listCollections(): Try[List[String]] =
      cache.listCollections()
