package gitinsp.infrastructure.factories

import dev.langchain4j.model.ollama.OllamaChatModel
import gitinsp.domain.interfaces.infrastructure.QueryRoutingStrategy
import gitinsp.infrastructure.strategies.ConditionalQueryStrategy
import gitinsp.infrastructure.strategies.DefaultQueryStrategy

import java.util.Locale

/** Factory for creating query routing strategy implementations. */
object QueryRoutingStrategyFactory:

  /** Creates a routing strategy based on the specified strategy type.
    *
    * @param strategyType The type of strategy to create
    * @param modelRouter The chat model to use (if needed)
    * @return A QueryRoutingStrategy implementation
    */
  def createStrategy(strategyType: String, modelRouter: OllamaChatModel): QueryRoutingStrategy =
    strategyType.toLowerCase(Locale.ROOT) match
      case "conditional" => ConditionalQueryStrategy(modelRouter)
      case "default"     => DefaultQueryStrategy()
      case _             => DefaultQueryStrategy() // Default fallback
