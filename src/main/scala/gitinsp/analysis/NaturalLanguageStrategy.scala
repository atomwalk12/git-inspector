package gitinsp.analysis

class NaturalLanguageStrategy extends AnalysisStrategy:

  override def analyze(code: String): Unit =
    println("Analyzing code")

  override def name: String = "NaturalLanguageStrategy"
