package gitinsp.domain.interfaces.application
import gitinsp.domain.models.GitRepository

import scala.util.Try

trait IngestorService:
  def ingest(repository: GitRepository): Try[Unit]
  def deleteRepository(repository: GitRepository): Try[Unit]
  def listCollections(): Try[List[String]]
