package gitinsp.tests

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import gitinsp.analysis.*

class AnalysisTest extends AnyFlatSpec with Matchers:

  "The User" should "be able to analyze code" in {
    val analysis = AnalysisContext.withCodeAnalysisStrategy(CodeAnalysisStrategy())
    analysis.strategy.map(_.name) should be(Some("CodeAnalysisStrategy"))
  }

  it should "be able to analyze natural language" in {
    val analysis = AnalysisContext.withNaturalLanguageStrategy(NaturalLanguageStrategy())
    analysis.strategy.map(_.name) should be(Some("NaturalLanguageStrategy"))
  }
