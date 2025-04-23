package gitinsp.infrastructure.factories

import gitinsp.domain.interfaces.application.QueryFilterStrategy
import gitinsp.infrastructure.strategies.LLMClassificationFilterStrategy
import gitinsp.infrastructure.strategies.NoFilterStrategy

import java.util.Locale

/** Factory for creating query filter strategy implementations. */
object QueryFilterStrategyFactory:

  /** Creates a filter strategy based on the specified strategy type.
    *
    * @param strategyType The type of strategy to create
    * @return A QueryFilterStrategy implementation
    */
  def createStrategy(strategyType: String): QueryFilterStrategy =
    strategyType.toLowerCase(Locale.ROOT) match
      case "llm" | "classification" => new LLMClassificationFilterStrategy()
      case "none" | "no-filter"     => new NoFilterStrategy()
      case _                        => new NoFilterStrategy() // Default fallback
