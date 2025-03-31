package gitinsp.analysis

class CodeAnalysisStrategy extends AnalysisStrategy:

  override def analyze(code: String): Unit =
    println("Analyzing code")

  override def name: String = "CodeAnalysisStrategy"
