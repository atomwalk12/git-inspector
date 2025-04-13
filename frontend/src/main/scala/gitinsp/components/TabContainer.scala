package gitinsp.components

import com.raquo.laminar.api.L.*

object TabContainer:
  case class Tab(id: String, label: String)

  def apply(
    tabs: Seq[Tab],
    selectedTabVar: Var[String],
    tabContent: Signal[HtmlElement],
  ): HtmlElement =
    div(
      cls := "tab-container",
      div(
        cls := "tab-header",
        tabs.map {
          tab =>
            button(
              // Set the CSS class for the header
              cls := "tab-button",
              // When the state changes, we activate the correct tab
              cls.toggle("active") <-- selectedTabVar.signal.map(_ == tab.id),
              span(tab.label),
              // On click, we change the contents of the selected variable
              // which in turn changes the contents of the tab from the caller
              onClick --> (_ => selectedTabVar.set(tab.id)),
            )
        },
      ),
      div(
        cls := "tab-content",
        child <-- tabContent,
      ),
    )
