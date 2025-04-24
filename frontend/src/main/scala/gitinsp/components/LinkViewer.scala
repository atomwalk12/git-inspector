package gitinsp.components
import com.raquo.laminar.api.L.*
import gitinsp.models.GenerateIndex
import gitinsp.services.ContentService

import scala.concurrent.ExecutionContext
import scala.util.Failure
import scala.util.Success
object LinkViewer:
  // Possible context formats
  val contextFormats   = List("Github")
  val statusVar        = Var("Idle...")
  val urlVar           = Var("")
  val contextFormatVar = Var("Github")
  val extensionVar     = Var("")
  val contentVar       = Var("")
  val isFetchingVar    = Var(false)
  val isGeneratingVar  = Var(false)

  def apply(
    contentService: ContentService,
    onIndexGenerated: Observer[String],
  )(implicit ec: ExecutionContext): HtmlElement =
    // Fetch content function defined within closure to access the variables
    def fetchContent: Observer[Unit] =
      Observer[Unit] {
        _ =>
          val validationCheck =
            for
              url <- Option(urlVar.now()).filter(_.nonEmpty)
              canFetch = !isFetchingVar.now()
            yield (url, canFetch)

          validationCheck match
            case Some((url, true)) =>
              isFetchingVar.set(true)
              statusVar.set("Fetching content...")

              val future = contentService.fetchContent(
                urlVar.now(),
                contextFormatVar.now(),
                extensionVar.now(),
              )

              future.onComplete {
                case Success(response) =>
                  contentVar.set(response)
                  statusVar.set("Operation completed.")
                  isFetchingVar.set(false)
                case Failure(error) =>
                  statusVar.set(s"Error: ${error.getMessage}")
                  isFetchingVar.set(false)
              }

            case Some((_, false)) =>
              statusVar.set("Please wait for the previous operation to complete")

            case None =>
              statusVar.set("Please enter a URL")
      }

    def generateIndex: Observer[Unit] =
      Observer[Unit] {
        _ =>
          val validationCheck =
            for
              url       <- Option(urlVar.now()).filter(_.nonEmpty)
              extension <- Option(extensionVar.now()).filter(_.nonEmpty)
              canGenerate = !isGeneratingVar.now()
            yield (url, extension, canGenerate)

          validationCheck match
            case Some((url, extension, true)) =>
              isGeneratingVar.set(true)
              statusVar.set("Generating index...")

              val params = GenerateIndex(url, extension)
              val future = contentService.generateIndex(params, Map.empty)

              future.onComplete {
                case Success(result) =>
                  statusVar.set(s"Index generated successfully. ID: ${result.indexName}")
                  onIndexGenerated.onNext(result.indexName)
                  isGeneratingVar.set(false)
                case Failure(error) =>
                  statusVar.set(s"Error generating index: no languages detected")
                  isGeneratingVar.set(false)
              }(ec)

            case Some((_, _, false)) =>
              statusVar.set("Please wait for the previous operation to complete")

            case None =>
              statusVar.set(s"Please enter a URL and at least one extension.")
      }

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
        onClick.mapTo(()) --> fetchContent,
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
        onClick.mapTo(()) --> generateIndex,
      ),

      // Status Section
      StatusBar(statusVar.signal),
    )
