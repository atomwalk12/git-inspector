package gitinsp.domain.models

import dev.langchain4j.service.TokenStream

/** Interface required by the AIService to work.
  * Provides chat functionality for interacting with a large language model.
  */
trait Assistant:
  /** Processes a user message and returns a token stream response
    * @param msg The message from the user to process
    * @return A stream of tokens representing the assistant's response
    */
  def chat(msg: String): TokenStream
