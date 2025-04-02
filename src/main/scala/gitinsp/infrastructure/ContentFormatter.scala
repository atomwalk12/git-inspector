package gitinsp.infrastructure

import com.typesafe.config.Config
import dev.langchain4j.model.ollama.OllamaChatModel
import dev.langchain4j.rag.query.Query
import dev.langchain4j.store.embedding.filter.Filter

/** Service for formatting content in different ways, particularly for converting
  * plain text to HTML for display in web interfaces.
  */
trait ContentFormatter:
  /** Format content as HTML with proper styling
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

object ContentFormatter extends ContentFormatter:
  override def toHtml(content: String): String =
    val htmlSafeContent = content.replace("\n", "<br>")
    s"<pre style=\"white-space: pre-wrap;\">$htmlSafeContent</pre>"

  override def toPlainText(content: String): String = content + "\n"

  @SuppressWarnings(Array("org.wartremover.warts.Null"))
  override def applyDynamicFilter(
    query: Query,
    config: Config,
    modelRouter: OllamaChatModel,
  ): Filter =
    Option
      .when(config.getBoolean("gitinsp.rag.use-dynamic-filter")) {
        Option
          .when(config.getBoolean("gitinsp.rag.use-dynamic-filter")) {
            null
          }
          .orNull
      }
      .orNull

  def docTemplate(num: Int, text: String): String =
    text // TODO: Implement doc template
