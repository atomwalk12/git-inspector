package gitinsp.domain.interfaces.infrastructure

import com.typesafe.config.Config
import gitinsp.domain.models.Language

trait IngestionStrategyFactory:
  def createStrategy(name: String, language: Language, config: Config): IngestionStrategy
