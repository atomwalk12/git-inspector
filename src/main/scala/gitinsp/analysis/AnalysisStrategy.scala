package gitinsp.analysis

trait AnalysisStrategy:
  def analyze(code: String): Unit

  def name: String
