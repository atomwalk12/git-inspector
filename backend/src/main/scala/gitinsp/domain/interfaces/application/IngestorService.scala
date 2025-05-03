package gitinsp.domain.interfaces.application
import gitinsp.domain.models.GitRepository

import scala.util.Try

/** Service responsible for ingesting and managing repository content in the RAG system
  * Handles the processes of adding, removing, and listing repository collections
  */
trait IngestorService:
  /** Ingests a repository's content into the RAG system for AI retrieval
    * @param repository The Git repository to ingest
    * @return A Try indicating success or failure of the ingestion process
    */
  def ingest(repository: GitRepository): Try[Unit]

  /** Removes a repository's content from the RAG system
    * @param repository The Git repository to delete from the system
    * @return A Try indicating success or failure of the deletion process
    */
  def deleteRepository(repository: GitRepository): Try[Unit]

  /** Retrieves a list of all available collections in the system
    * @return A list of collection names wrapped in a Try
    */
  def listCollections(): Try[List[String]]
