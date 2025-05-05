package gitinsp.components

import com.raquo.laminar.api.L.*
import gitinsp.models.IndexOption
object IndexSelector:
  def apply(
    availableIndexesSignal: Signal[Seq[IndexOption]], // the available indexes
    selectedIndexVar: Var[String],                    // the current selected index
    onRefreshRequested: Observer[Unit],               // the callback to execute on refresh
    onRemoveRequested: Observer[Unit],
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
        cls("loading-button") <-- isLoadingVar.signal,
        "Remove Index",
        disabled <-- isLoadingVar.signal,

        // Execute the callback when the button is clicked
        onClick.mapTo(()) --> onRemoveRequested,
      ),
      button(
        cls := "load-index-button",
        cls("loading-button") <-- isLoadingVar.signal,
        child.text <-- isLoadingVar.signal.map(
          loading =>
            if loading then "Loading..." else "Load Indices",
        ),
        disabled <-- isLoadingVar.signal,

        // Execute the callback when the button is clicked
        onClick.mapTo(()) --> onRefreshRequested,
      ),
    )
