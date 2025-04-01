package gitinsp.analysis

import akka.NotUsed
import akka.stream.scaladsl.Source
import gitinsp.utils.StreamingAssistant

import scala.concurrent.Future

/** Context class that manages different code analysis strategies.
  * This class implements the Strategy pattern using immutable state.
  *
  * @param strategy The strategy to use for code analysis
  */
final case class AnalysisContext(
  strategy: Option[AnalysisStrategy],
):

  /** Returns a new context with the specified strategy
    *
    * @param newStrategy The strategy to use
    * @return a new context instance with the specified strategy
    */
  def withStrategy(newStrategy: AnalysisStrategy): AnalysisContext =
    copy(strategy = Some(newStrategy))

  /** Analyzes code using the current strategy
    *
    * @param query The user's question about the code
    * @param codeContext The code to analyze
    * @return Future containing the analysis result as a streaming Source
    * @throws IllegalStateException if no strategy is set
    */
  def analyzeCode(query: String, codeContext: String): Future[Source[String, NotUsed]] =
    strategy match
      case Some(s) => s.analyze(query, codeContext)
      case None    => Future.failed(new IllegalStateException("No analysis strategy set"))

/** Companion object providing factory methods to create contexts with specific strategies */
object AnalysisContext:

  /** Creates an empty context with no strategy */
  def apply(): AnalysisContext =
    new AnalysisContext(None)

  /** Factory method to create a new context with natural language strategy */
  def withNaturalLanguageStrategy(assistant: StreamingAssistant): AnalysisContext =
    apply().withStrategy(new NaturalLanguageStrategy(assistant))

  /** Factory method to create a new context with code analysis strategy */
  def withCodeAnalysisStrategy(assistant: StreamingAssistant): AnalysisContext =
    apply().withStrategy(new CodeAnalysisStrategy(assistant))
