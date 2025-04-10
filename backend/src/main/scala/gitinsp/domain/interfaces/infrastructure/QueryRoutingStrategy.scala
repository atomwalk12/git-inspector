package gitinsp.domain.interfaces.infrastructure
import dev.langchain4j.rag.content.retriever.ContentRetriever
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever
import dev.langchain4j.rag.query.Query

import java.util.Collection

/** Strategy interface for determining which retrievers to use for a given query.
  * Enables different routing strategies to be implemented and swapped at runtime.
  */
trait QueryRoutingStrategy:

  /** Determines which retrievers to use for a given query
    *
    * @param query The user query
    * @param retrievers List of available retrievers
    * @return Collection of content retrievers to use
    */
  def determineRetrievers(
    query: Query,
    retrievers: List[EmbeddingStoreContentRetriever],
  ): Collection[ContentRetriever]
