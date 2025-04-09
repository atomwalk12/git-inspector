package gitinsp.domain.interfaces.application

import com.typesafe.config.Config
import dev.langchain4j.model.ollama.OllamaChatModel
import dev.langchain4j.rag.query.Query
import dev.langchain4j.store.embedding.filter.Filter

trait QueryFilterService:
  def applyDynamicFilter(query: Query, config: Config, modelRouter: OllamaChatModel): Filter
