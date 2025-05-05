package gitinsp.domain.interfaces.application

import dev.langchain4j.model.ollama.OllamaChatModel
import dev.langchain4j.rag.query.Query
import dev.langchain4j.store.embedding.filter.Filter

/** Strategy interface for creating query filters.
  * This allows for different filtering strategies to be implemented at runtime.
  */
trait QueryFilterStrategy:
  /** Creates a filter based on the given query
    *
    * @param query The user query
    * @param modelRouter The chat model to use for classification
    * @return Optional filter to apply to the query
    */
  def createFilter(query: Query, modelRouter: OllamaChatModel): Option[Filter]
