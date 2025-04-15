package gitinsp.components
import com.raquo.laminar.api.L.*
import gitinsp.services.ContentService

object LinkViewer:
  // Possible context formats
  val contextFormats = List("Github")
  val statusVar      = Var("Idle...")

  def apply(
    contentService: ContentService,
    onIndexGenerated: Observer[String],
  ): HtmlElement =
    val urlVar           = Var("")
    val contextFormatVar = Var("Github")
    val extensionVar     = Var("")
    val contentVar       = Var("")
    div(
      cls := "link-viewer-container",

      // URL & Format Input Section
      div(
        cls := "input-section",

        // URL input panel
        div(
          cls := "input-group",
          label(cls := "input-label", "Enter URL"),
          input(
            cls         := "url-input",
            placeholder := "https://github.com/user/repo",
            controlled(
              value <-- urlVar.signal,
              onInput.mapToValue --> urlVar,
            ),
          ),
        ),

        // Context Format panel
        div(
          cls := "input-group",
          label(cls := "input-label", "Context Format"),
          select(
            cls := "format-select",
            controlled(
              value <-- contextFormatVar.signal,
              onChange.mapToValue --> contextFormatVar,
            ),
            contextFormats.map(
              format =>
                option(value := format, format),
            ),
          ),
        ),

        // Extension panel
        div(
          cls := "input-group",
          label(cls := "input-label", "Extension"),
          input(
            cls         := "extension-input",
            placeholder := "e.g., scala.md",
            controlled(
              value <-- extensionVar.signal,
              onInput.mapToValue --> extensionVar,
            ),
          ),
        ),
      ),

      // Fetch Content Button
      button(
        cls := "fetch-button",
        "Fetch Content",
        onClick.mapTo(()) --> fetchContent(),
      ),

      // Content Display Area
      div(
        cls := "content-container",
        h3(cls := "content-header", "Link Content"),
        pre(
          cls := "content-display",
          child.text <-- contentVar,
        ),
      ),

      // Generate Index Button
      button(
        cls := "generate-index-button",
        "Generate Index",
        onClick.mapTo(()) --> generateIndex(),
      ),

      // Status Section
      StatusBar(statusVar.signal),
    )

  def fetchContent(): Observer[Unit] =
    Observer[Unit] { _ => statusVar.set("Fetching content...") }

  def generateIndex(): Observer[Unit] =
    Observer[Unit] { _ => statusVar.set("Generating index...") }
