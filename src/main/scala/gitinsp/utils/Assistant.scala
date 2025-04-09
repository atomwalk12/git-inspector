package gitinsp.utils

import dev.langchain4j.service.TokenStream

// This interface is required by the AIService to work
trait Assistant:
  def chat(msg: String): TokenStream
