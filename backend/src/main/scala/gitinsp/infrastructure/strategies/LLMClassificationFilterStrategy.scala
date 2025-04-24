package gitinsp.infrastructure.strategies

import dev.langchain4j.model.input.PromptTemplate
import dev.langchain4j.model.ollama.OllamaChatModel
import dev.langchain4j.rag.query.Query
import dev.langchain4j.store.embedding.filter.Filter
import dev.langchain4j.store.embedding.filter.MetadataFilterBuilder.metadataKey
import gitinsp.domain.interfaces.application.QueryFilterStrategy
import gitinsp.domain.models.Category
import gitinsp.domain.models.Language

import java.util.Locale

/** A filter strategy that uses an LLM to classify the query and determine
  * which programming language is being requested, then filters results accordingly.
  */
class LLMClassificationFilterStrategy extends QueryFilterStrategy:
  private val promptTemplate =
    """You are a classifier that determines if a query is requesting Python, Java, C++, or Scala code.
      |
      |QUERY: {{it}}
      |
      |INSTRUCTIONS:
      |1. If the query is requesting code, respond with exactly "yes: <extension>" where extension is one of:
      |   - "py" for Python
      |   - "java" for Java
      |   - "cpp" for C++
      |   - "scala" for Scala
      |2. If the query is not requesting code, respond with exactly "no"
      |
      |Your entire response must be either "yes: py", "yes: java", "yes: cpp", "yes: scala", or "no".
      |Do not include any other text in your response.""".stripMargin

  override def createFilter(query: Query, modelRouter: OllamaChatModel): Option[Filter] =
    val prompt = PromptTemplate.from(promptTemplate)
      .apply(query.metadata().userMessage().text())

    // This retrieves the response from the model
    val response = modelRouter
      .chat(prompt.toUserMessage())
      .aiMessage()
      .text()

    // Here, I must use a null value because the Java backend expects this
    // when no filter should be applied.
    parseResponse(response)

  private def parseResponse(response: String): Option[Filter] =
    val trimmedResponse = response.trim.toLowerCase(Locale.ROOT)

    // Here a model classifies the query and ensures that code of a specific language is retrieved
    trimmedResponse match
      case "no"                       => Option.empty[Filter]
      case r if r.startsWith("yes: ") => createLanguageFilter(r.substring(5).trim)
      case unexpected =>
        println(s"Unexpected response format: $unexpected")
        Option.empty[Filter]

  private def createLanguageFilter(extension: String): Option[Filter] = extension match
    // Filter to retrieve only specific code files when multiple file types exist in the index
    case Language.PYTHON.value =>
      Some(metadataKey(Category.CODE.value).isEqualTo(Language.PYTHON.value))
    case Language.JAVA.value =>
      Some(metadataKey(Category.CODE.value).isEqualTo(Language.JAVA.value))
    case Language.CPP.value =>
      Some(metadataKey(Category.CODE.value).isEqualTo(Language.CPP.value))
    case Language.SCALA.value =>
      Some(metadataKey(Category.CODE.value).isEqualTo(Language.SCALA.value))
    case _ => Option.empty[Filter]
