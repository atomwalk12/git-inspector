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

/** Service for managing vector database collections and AI services
  * Provides caching and lifecycle management for RAG components
  */
trait CacheService extends LazyLogging:
  /** The Qdrant vector database client used for storing embeddings
    * @return A configured QdrantClient instance
    */
  def qdrantClient: QdrantClient

  /** Lists all available collections in the vector database
    * @return A list of collection names wrapped in a Try
    */
  def listCollections(): Try[List[String]]

  /** Initializes or retrieves an AI service for a repository
    * @param repository Optional repository with categories to create services for
    * @return An initialized Assistant ready for interaction
    */
  def initializeAIServices(repository: Option[RepositoryWithCategories]): Assistant

  /** Creates an ingestor for adding documents to the vector database
    * @param index The target Qdrant index URL
    * @param language The programming language of the content
    * @param strategy Strategy for processing and ingesting documents
    * @return An EmbeddingStoreIngestor configured for the specified parameters
    */
  def getIngestor(index: QdrantURL, language: Language, strategy: IngestionStrategy): Ingestor

  /** The RAG component factory used to create various components
    * @return The configured RAGComponentFactory instance
    */
  def factory: RAGComponentFactory

  /** Creates a new collection in the vector database
    * @param name The name of the collection to create
    * @param distance The distance metric to use for similarity calculations
    * @return A Try indicating success or failure of the operation
    */
  def createCollection(name: String, distance: Distance): Try[Unit]

  /** Retrieves an AI service by its index URL
    * @param index The URL identifying the AI service
    * @return The requested Assistant wrapped in a Try
    */
  def getAIService(index: AIServiceURL): Try[Assistant]

  /** Deletes a collection from the vector database
    * @param indexName The URL of the collection to delete
    * @return A Try indicating success or failure of the deletion
    */
  def deleteCollection(indexName: QdrantURL): Try[Unit]

  /** Removes an AI service from the cache
    * @param indexName The URL of the AI service to delete
    * @return A Try indicating success or failure of the deletion
    */
  def deleteAIService(indexName: AIServiceURL): Try[Unit]

  /** Core implementation of collection deletion
    * @param index The URL of the collection to delete
    * @return A Try indicating success or failure of the deletion
    */
  def delete(index: QdrantURL): Try[Unit]
