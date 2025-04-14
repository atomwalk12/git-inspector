package gitinsp.components

import com.raquo.laminar.api.L.*

case class IndexOption(id: String, label: String)

object IndexSelector:
  def apply(
    availableIndexesSignal: Signal[Seq[IndexOption]], // the available indexes
    selectedIndexVar: Var[String],                    // the current selected index
    onRefreshRequested: Observer[Unit],               // the callback to execute on refresh
  ): HtmlElement =

    val isLoadingVar = Var(false)
    div(
      cls := "index-selector",
      div(
        cls := "current-index",
        label(cls := "input-label", "Current Index"),
        select(
          cls := "index-dropdown",
          // Listen to changes in the dropdown and propagate to caller
          controlled(
            value <-- selectedIndexVar.signal,
            onChange.mapToValue --> selectedIndexVar,
          ),
          // Map available indexes to options
          children <-- availableIndexesSignal.map {
            opts =>
              opts.map {
                opt =>
                  option(
                    value := opt.id,
                    opt.label,
                  )
              }
          },
        ),
      ),
      button(
        cls := "load-index-button",

        // TODO: the time it takes to load the indices is short, so this may not be needed
        cls("loading-button") <-- isLoadingVar.signal,
        child.text <-- isLoadingVar.signal.map {
          case true  => "Loading..."
          case false => "Load Indices"
        },
        disabled <-- isLoadingVar.signal,

        // Execute the callback when the button is clicked
        onClick.mapTo(()) --> onRefreshRequested,
      ),
    )
