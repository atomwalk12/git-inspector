package gitinsp.domain.interfaces.application

import com.typesafe.config.Config
import dev.langchain4j.model.ollama.OllamaChatModel
import dev.langchain4j.rag.query.Query
import dev.langchain4j.store.embedding.filter.Filter

/** Service responsible for dynamically filtering vector store queries based on user input
  * This allows for more targeted retrieval of information from the RAG system
  */
trait QueryFilterService:
  /** Applies a dynamic filter to a query based on the content of the query and configuration settings
    * @param query The user query to analyze for filtering
    * @param config Configuration containing filter settings and strategies
    * @param modelRouter The AI model used to understand query intent for filtering
    * @return A Filter object that can be applied to vector store searches, or null if no filtering should be applied
    */
  def applyDynamicFilter(query: Query, config: Config, modelRouter: OllamaChatModel): Filter
