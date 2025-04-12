package gitinsp.domain.interfaces.application
import gitinsp.domain.models.RepositoryWithLanguages

import scala.util.Try

trait IngestorService:
  def ingest(repository: RepositoryWithLanguages): Try[Unit]
  def deleteRepository(repository: RepositoryWithLanguages): Try[Unit]
  def listCollections(): Try[List[String]]
