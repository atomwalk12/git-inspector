package gitinsp.tests

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import gitinsp.analysis.*
import gitinsp.utils.StreamingAssistant

class AnalysisTest extends AnyFlatSpec with Matchers:

  "The Analysis Context" should "be able to analyze code" in {
    val analysis = AnalysisContext.withCodeAnalysisStrategy(StreamingAssistant())
    analysis.strategy.map(_.strategyName) should be(Some("Code Analysis"))
  }
  it should "be able to analyze natural language" in {
    val analysis = AnalysisContext.withNaturalLanguageStrategy(StreamingAssistant())
    analysis.strategy.map(_.strategyName) should be(Some("Markdown Analysis"))
  }
