package gitinsp.analysis

import akka.NotUsed
import akka.stream.scaladsl.Source
import gitinsp.utils.Assistant

import scala.concurrent.Future

@SuppressWarnings(Array("org.wartremover.warts.ImplicitParameter"))
/** Strategy implementation for analyzing natural language.
  * Specifically, it is optimized for analyzing markdown text.
  */
class NaturalLanguageStrategy(assistant: Assistant) extends AnalysisStrategy:
  override def analyze(query: String, codeContext: String): Future[Source[String, NotUsed]] =
    Future.successful(Source.empty)

  override def strategyName: String = "Markdown Analysis"
