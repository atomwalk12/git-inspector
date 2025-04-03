package gitinsp.analysis

import akka.NotUsed
import akka.stream.scaladsl.Source
import gitinsp.utils.Assistant

import scala.concurrent.Future

@SuppressWarnings(Array("org.wartremover.warts.ImplicitParameter"))
/** Strategy implementation specialized for code analysis.
  * This strategy assumes that the underlying text represents a codebase.
  */
class CodeAnalysisStrategy(assistant: Assistant) extends AnalysisStrategy:

  override def analyze(query: String, codeContext: String): Future[Source[String, NotUsed]] =
    Future.successful(Source.empty)

  override def strategyName: String = "Code Analysis"
