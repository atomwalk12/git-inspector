package gitinsp.domain.interfaces.application
import gitinsp.domain.models.AIServiceURL
import gitinsp.domain.models.Assistant
import gitinsp.domain.models.Category
import gitinsp.domain.models.GitRepository
import gitinsp.domain.models.Language
import gitinsp.domain.models.StreamedResponse
import gitinsp.domain.models.URL

import scala.util.Try

/** Core service orchestrating the workflow of the Git Inspector application
  * Coordinates interactions between various services including chat, repository indexing,
  * content retrieval, and AI service management
  */
trait Pipeline:
  /** Processes a user message with the specified AI assistant
    * @param message The message to be processed
    * @param aiService The AI assistant implementation to use
    * @return A streaming response with the AI-generated content
    */
  def chat(message: String, aiService: Assistant): StreamedResponse

  /** Generates or regenerates an index for a repository
    * @param repository The Git repository to index
    * @param regenerate Whether to delete existing index before generating a new one
    * @return A Try indicating success or failure of the operation
    */
  def generateIndex(repository: GitRepository, regenerate: Boolean): Try[Unit]

  /** Regenerates an index for a repository by first deleting the existing one
    * @param repository The Git repository to reindex
    * @return A Try indicating success or failure of the operation
    */
  def regenerateIndex(repository: GitRepository): Try[Unit]

  /** Retrieves a list of all available AI service indexes
    * @return A list of AI service URLs wrapped in a Try
    */
  def listIndexes(): Try[List[AIServiceURL]]

  /** Builds a repository model from a URL and a list of languages
    * @param url The URL of the repository to build
    * @param languages The programming languages to filter for
    * @return A Git repository model wrapped in a Try
    */
  def buildRepository(url: URL, languages: List[Language]): Try[GitRepository]

  /** Fetches content from a repository
    * @param url The URL of the repository to fetch
    * @param languages The programming languages to filter for
    * @return A string containing the repository content wrapped in a Try
    */
  def fetchRepository(url: URL, languages: List[Language]): Try[String]

  /** Deletes an index by URL and category
    * @param index The URL of the index to delete
    * @param category The category (CODE or TEXT) of the index to delete
    * @return A Try indicating success or failure of the deletion
    */
  def deleteIndex(index: URL, category: Category): Try[Unit]

  /** Retrieves an AI assistant service for a given index
    * @param index Optional AI service URL to use (uses default if None)
    * @return An AI assistant implementation wrapped in a Try
    */
  def getAIService(index: Option[AIServiceURL]): Try[Assistant]
