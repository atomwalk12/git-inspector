package gitinsp.domain.interfaces.infrastructure

import com.typesafe.scalalogging.LazyLogging
import gitinsp.domain.models.GitRepository
import gitinsp.domain.models.Language
import gitinsp.domain.models.URL

import scala.util.Try

/** Service for interacting with GitHub repositories
  * Provides functionality to fetch and build repository models for indexing and content retrieval
  */
trait GithubWrapperService extends LazyLogging:
  /** Creates a structured repository model from a GitHub URL
    * @param url The URL of the GitHub repository
    * @param languages List of programming languages to filter repository content
    * @return A GitRepository model ready for indexing wrapped in a Try
    */
  def buildRepository(url: URL, languages: List[Language]): Try[GitRepository]

  /** Fetches raw content from a GitHub repository
    * @param url The URL of the GitHub repository
    * @param languages List of programming languages to filter repository content
    * @return Repository content as a string wrapped in a Try
    */
  def fetchRepository(url: URL, languages: List[Language]): Try[String]
