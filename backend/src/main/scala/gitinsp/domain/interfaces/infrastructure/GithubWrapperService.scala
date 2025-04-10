package gitinsp.domain.interfaces.infrastructure

import com.typesafe.scalalogging.LazyLogging
import gitinsp.domain.models.Language
import gitinsp.domain.models.RepositoryWithLanguages
import gitinsp.domain.models.URL

import scala.util.Try

trait GithubWrapperService extends LazyLogging:
  def buildRepository(url: URL, languages: List[Language]): Try[RepositoryWithLanguages]
  def fetchRepository(url: URL, languages: List[Language]): Try[String]
