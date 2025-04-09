package gitinsp.domain.interfaces.infrastructure

import com.typesafe.scalalogging.LazyLogging
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor as Ingestor
import gitinsp.domain.models.AIServiceURL
import gitinsp.domain.models.Assistant
import gitinsp.domain.models.Language
import gitinsp.domain.models.QdrantURL
import gitinsp.domain.models.RepositoryWithCategories
import io.qdrant.client.QdrantClient
import io.qdrant.client.grpc.Collections.Distance

import scala.util.Try

trait CacheService extends LazyLogging:
  def qdrantClient: QdrantClient
  def listCollections(): Try[List[String]]
  def initializeAIServices(repository: Option[RepositoryWithCategories]): Assistant
  def getIngestor(index: QdrantURL, language: Language, strategy: IngestionStrategy): Ingestor
  def factory: RAGComponentFactory
  def createCollection(name: String, distance: Distance): Try[Unit]
  def getAIService(index: AIServiceURL): Try[Assistant]
  def deleteCollection(indexName: QdrantURL): Try[Unit]
  def deleteAIService(indexName: AIServiceURL): Try[Unit]
  def delete(index: QdrantURL): Try[Unit]
