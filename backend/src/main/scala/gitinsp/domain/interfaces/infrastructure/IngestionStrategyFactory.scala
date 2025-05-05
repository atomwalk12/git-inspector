package gitinsp.domain.interfaces.infrastructure

import com.typesafe.config.Config
import gitinsp.domain.models.Language

/** Factory for creating document ingestion strategies
  * Provides a mechanism to select and configure different document processing approaches
  */
trait IngestionStrategyFactory:
  /** Creates an ingestion strategy based on the specified parameters
    * @param name The name/type of strategy to create
    * @param language The programming language the strategy will process
    * @param config Configuration containing strategy parameters
    * @return An IngestionStrategy configured for the specified language and settings
    */
  def createStrategy(name: String, language: Language, config: Config): IngestionStrategy
