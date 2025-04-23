package gitinsp.infrastructure.strategies

import dev.langchain4j.model.ollama.OllamaChatModel
import dev.langchain4j.rag.query.Query
import dev.langchain4j.store.embedding.filter.Filter
import gitinsp.domain.interfaces.application.QueryFilterStrategy

/** A simple filter strategy that never applies any filter to the query.
  * This is useful as a fallback or default strategy.
  */
class NoFilterStrategy extends QueryFilterStrategy:
  override def createFilter(query: Query, modelRouter: OllamaChatModel): Option[Filter] =
    Option.empty[Filter]
