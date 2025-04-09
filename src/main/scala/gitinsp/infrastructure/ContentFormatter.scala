package gitinsp.infrastructure

import com.typesafe.config.Config
import dev.langchain4j.model.input.PromptTemplate
import dev.langchain4j.model.ollama.OllamaChatModel
import dev.langchain4j.rag.query.Query
import dev.langchain4j.store.embedding.filter.Filter
import dev.langchain4j.store.embedding.filter.MetadataFilterBuilder.metadataKey
import gitinsp.utils.Category
import gitinsp.utils.Language

import java.util.Locale

/** Service for formatting content in different ways, particularly for converting
  * plain text to HTML for display in web interfaces.
  */
trait ContentFormatter:
  /** Format content as HTML with proper styling.
    *
    * @param content The plain text content to format
    * @return HTML formatted string
    */
  def toHtml(content: String): String

  /** Format content as plain text with minimal formatting
    *
    * @param content The content to format
    * @return Plain text formatted string
    */
  def toPlainText(content: String): String

  /** Apply a dynamic filter to a query
    *
    * @param query The query to filter
    * @return A Filter
    */
  def applyDynamicFilter(query: Query, config: Config, modelRouter: OllamaChatModel): Filter

  /** Format a document with a template
    *
    * @param docNumber The document number
    * @param text The document text
    * @return Formatted document
    */
  def docTemplate(docNumber: Int, text: String): String

object ContentFormatter extends ContentFormatter:

  override def toHtml(content: String): String =
    // Used to improve readability of the retrieved content in the UI
    val htmlSafeContent = content.replace("\n", "<br>")
    s"<pre style=\"white-space: pre-wrap;\">$htmlSafeContent</pre>"

  override def toPlainText(content: String): String = content + "\n"

  @SuppressWarnings(Array("org.wartremover.warts.Null"))
  override def applyDynamicFilter(
    query: Query,
    config: Config,
    modelRouter: OllamaChatModel,
  ): Filter =
    // This must return null because the Java backend expects this
    // when no filter should be applied.
    Option
      .when(config.getBoolean("gitinsp.rag.use-dynamic-filter")) {
        dynamicFilter(query, config, modelRouter)
      }
      .orNull

  @SuppressWarnings(Array("org.wartremover.warts.Null"))
  private def dynamicFilter(query: Query, config: Config, modelRouter: OllamaChatModel): Filter =
    val promptTemplate =
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

    def createFilter(extension: String): Option[Filter] = extension match
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

    def parseResponse(response: String): Option[Filter] =
      val trimmedResponse = response.trim.toLowerCase(Locale.ROOT)

      // Here a model classifies the query and ensures that code of a specific language is retrieved
      trimmedResponse match
        case "no"                       => Option.empty[Filter]
        case r if r.startsWith("yes: ") => createFilter(r.substring(5).trim)
        case unexpected =>
          println(s"Unexpected response format: $unexpected")
          Option.empty[Filter]

    val prompt = PromptTemplate.from(promptTemplate)
      .apply(query.metadata().userMessage().contents())

    // This retrieves the response from the model
    val response = modelRouter
      .chat(prompt.toUserMessage())
      .aiMessage()
      .text()

    // Here, I must use a null value because the Java backend expects this
    // when no filter should be applied.
    parseResponse(response).orNull

  override def docTemplate(num: Int, text: String): String =
    s"""Document ${"%02d".format(num)}
        ===========
        $text""".stripMargin
