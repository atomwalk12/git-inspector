package gitinsp.infrastructure

import gitinsp.domain.interfaces.infrastructure.ContentService

object ContentService extends ContentService:

  override def toHtml(content: String): String =
    // Used to improve readability of the retrieved content in the UI
    val htmlSafeContent = content.replace("\n", "<br>")
    s"<pre style=\"white-space: pre-wrap;\">$htmlSafeContent</pre>"

  override def toPlainText(content: String): String = content + "\n"

  override def docTemplate(num: Int, text: String): String =
    s"""Document ${"%02d".format(num)}\n===========\n$text\n\n""".stripMargin
