package gitinsp.chatpipeline

import dev.langchain4j.model.input.PromptTemplate
import dev.langchain4j.model.ollama.OllamaChatModel
import dev.langchain4j.rag.content.retriever.ContentRetriever
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever
import dev.langchain4j.rag.query.Query

import java.util.Collection
import java.util.Locale

import scala.jdk.CollectionConverters.*

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

/** Strategy that conditionally uses retrievers based on LLM classification of the query.
  * Uses an LLM to determine if the query explicitly asks not to use RAG.
  */
class ConditionalQueryStrategy(modelRouter: OllamaChatModel) extends QueryRoutingStrategy:
  private val PROMPT_TEMPLATE = PromptTemplate.from(
    "Did the user ask explicitly not to query the RAG index??" +
      "Answer only 'yes' or 'no'. " +
      "Query: {{it}}",
  )

  override def determineRetrievers(
    query: Query,
    retrievers: List[EmbeddingStoreContentRetriever],
  ): Collection[ContentRetriever] =
    val prompt    = PROMPT_TEMPLATE.apply(query.text())
    val aiMessage = modelRouter.chat(prompt.toUserMessage()).aiMessage()

    // This basically allows to bypass fetching the RAG index if the modelk asked explicitly not to
    if aiMessage.text().toLowerCase(Locale.ROOT).contains("no") then
      java.util.Collections.emptyList()
    else
      retrievers.asJava

/** Strategy that always uses all provided retrievers. */
class DefaultQueryStrategy extends QueryRoutingStrategy:
  override def determineRetrievers(
    query: Query,
    retrievers: List[EmbeddingStoreContentRetriever],
  ): Collection[ContentRetriever] =
    // Here if we'd like to avoid doing the check defined in the ConditionalQueryStrategy,
    // we simply return all retrievers
    retrievers.asJava
