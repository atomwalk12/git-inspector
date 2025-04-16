package gitinsp.components

import com.raquo.laminar.api.L.*

object StatusBar:
  def apply(statusSignal: Signal[String]): HtmlElement =
    div(
      cls := "status-section",
      h4(cls := "status-header", "Status"),

      // Set current status
      div(
        cls := "status-message",
        child.text <-- statusSignal,
      ),
    )
