package gitinsp.analysis

import akka.NotUsed
import akka.stream.scaladsl.Source

import scala.concurrent.Future

/** Base trait defining the contract for different code analysis strategies.
  * Implements the Strategy pattern for handling different types of code analysis.
  */
trait AnalysisStrategy {

  /** Analyzes the given query in the context of provided code.
    *
    * @param query The user's question or query about the code
    * @param codeContext The relevant code snippets to analyze
    * @return Future containing a Source of streaming responses
    */
  def analyze(query: String, codeContext: String): Future[Source[String, NotUsed]]

  /** Returns the name/identifier of this strategy */
  def strategyName: String
}
