package gitinsp.domain.interfaces.infrastructure

/** Service for formatting content in different ways, particularly for converting
  * plain text to HTML for display in web interfaces.
  */
trait ContentService:
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

  /** Format a document with a template
    *
    * @param docNumber The document number
    * @param text The document text
    * @return Formatted document
    */
  def docTemplate(docNumber: Int, text: String): String
