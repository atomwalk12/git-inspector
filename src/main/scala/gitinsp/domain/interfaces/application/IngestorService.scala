package gitinsp.domain.interfaces.application
import gitinsp.domain.models.RepositoryWithLanguages

import scala.util.Try

trait IngestorService:
  def ingest(repository: RepositoryWithLanguages): Unit
  def deleteRepository(repository: RepositoryWithLanguages): Unit
  def listCollections(): Try[List[String]]
