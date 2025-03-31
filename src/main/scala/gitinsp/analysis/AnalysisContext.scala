package gitinsp.analysis

import scala.concurrent.Future

final case class AnalysisContext(strategy: Option[AnalysisStrategy]):

  def withStrategy(newStrategy: AnalysisStrategy): AnalysisContext =
    copy(strategy = Some(newStrategy))

  def analyze(code: String): Unit =
    strategy match
      case Some(s) => s.analyze(code)
      case None    => Future.failed(new IllegalArgumentException("No strategy set"))

object AnalysisContext {

  def apply(): AnalysisContext =
    AnalysisContext(None)

  def withNaturalLanguageStrategy(strategy: NaturalLanguageStrategy): AnalysisContext = {
    apply().withStrategy(strategy)
  }

  def withCodeAnalysisStrategy(strategy: CodeAnalysisStrategy): AnalysisContext = {
    apply().withStrategy(strategy)
  }
}
