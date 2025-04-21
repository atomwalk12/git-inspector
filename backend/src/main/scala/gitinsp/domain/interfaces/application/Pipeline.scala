package gitinsp.domain.interfaces.application
import gitinsp.domain.models.AIServiceURL
import gitinsp.domain.models.Assistant
import gitinsp.domain.models.Category
import gitinsp.domain.models.GitRepository
import gitinsp.domain.models.Language
import gitinsp.domain.models.StreamedResponse
import gitinsp.domain.models.URL

import scala.util.Try

trait Pipeline:
  def chat(message: String, aiService: Assistant): StreamedResponse
  def generateIndex(repository: GitRepository, regenerate: Boolean): Try[Unit]
  def regenerateIndex(repository: GitRepository): Try[Unit]
  def listIndexes(): Try[List[AIServiceURL]]
  def buildRepository(url: URL, languages: List[Language]): Try[GitRepository]
  def fetchRepository(url: URL, languages: List[Language]): Try[String]
  def deleteIndex(index: URL, category: Category): Try[Unit]
  def getAIService(index: Option[AIServiceURL]): Try[Assistant]
