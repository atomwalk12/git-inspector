package gitinsp.infrastructure

import com.typesafe.config.Config
import dev.langchain4j.model.ollama.OllamaChatModel
import dev.langchain4j.rag.query.Query
import dev.langchain4j.store.embedding.filter.Filter
import gitinsp.domain.interfaces.application.QueryFilterService
import gitinsp.infrastructure.factories.QueryFilterStrategyFactory

/** Implementation of the QueryFilterService that uses the Strategy pattern
  * to determine which filter to apply to a query.
  */
object QueryFilterService extends QueryFilterService:
  @SuppressWarnings(Array("org.wartremover.warts.Null"))
  override def applyDynamicFilter(
    query: Query,
    config: Config,
    modelRouter: OllamaChatModel,
  ): Filter =

    val useFilter = config.getBoolean("gitinsp.rag.use-dynamic-filter")
    if useFilter then
      val strategyType = config.getString("gitinsp.rag.filter-strategy")
      val strategy     = QueryFilterStrategyFactory.createStrategy(strategyType)
      strategy.createFilter(query, modelRouter).orNull
    else
      // This must return null because the Java backend expects this
      // when no filter should be applied.
      null
