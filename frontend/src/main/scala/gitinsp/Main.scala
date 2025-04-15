package gitinsp

import com.raquo.laminar.api.L.*
import gitinsp.api.HttpClient
import gitinsp.components.ChatInterface
import gitinsp.components.ChatInterface.ChatMessage
import gitinsp.components.IndexOption
import gitinsp.components.IndexSelector
import gitinsp.components.LinkViewer
import gitinsp.components.TabContainer
import gitinsp.components.TabContainer.Tab
import gitinsp.models.IndexEvent
import gitinsp.models.IndexGenerated
import gitinsp.models.RefreshIndicesRequested
import gitinsp.services.ContentService
import org.scalajs.dom

import scala.concurrent.ExecutionContext.Implicits.global

object GitInspectorFrontend:

  // ================================
  // Application State
  // ================================

  // Available tabs
  private val tabs = Seq(Tab("chat", "Chat"), Tab("linkViewer", "Link Viewer"))

  // State management
  private val selectedTabVar: Var[String] = Var("chat")
  private val chatMessagesVar: Var[Seq[ChatMessage]] = Var(Seq(
    ChatMessage(id = "welcome", isBot = true, content = "Hello, how can I help you?"),
  ))
  private val availableIndexesVar = Var(Seq(
    IndexOption("None", "None"),
    IndexOption("index1", "Index 1"),
  ))
  private val selectedIndexVar: Var[String] = Var("None")
  private val chatStatusVar: Var[String]    = Var("Welcome to Git Inspector!")

  // The possible events are triggered when an index is generated and when the indices are refreshed
  private val indexEvents = new EventBus[IndexEvent]()

  // ================================
  // Services
  // ================================
  private val httpClient = HttpClient(baseUrl = "") // The base URL is implicitly managed by Vite
  private val contentService = new ContentService(httpClient)

  def main(args: Array[String]): Unit =
    // Initialize application
    setupEventListeners()
    refreshAvailableIndices()
    renderOnDomContentLoaded(dom.document.querySelector("#app"), appElement())

  // ================================
  // Event Handlers
  // ================================
  private def setupEventListeners(): Unit =
    // Subscribe to index events
    indexEvents.events.foreach {
      case IndexGenerated(name) =>
        chatStatusVar.set(s"New index generated: $name")
        refreshAvailableIndices()
      case RefreshIndicesRequested =>
        refreshAvailableIndices()
    }(unsafeWindowOwner)

  private def refreshAvailableIndices(): Unit =
    contentService
      .fetchAvailableIndices()
      .foreach(indices => availableIndexesVar.set(indices))(global)

  // ================================
  // UI Components
  // ================================
  private def appElement(): HtmlElement =
    div(
      cls := "app-container",
      TabContainer(
        tabs = tabs,
        selectedTabVar = selectedTabVar,

        // When a new tab is selected, render the corresponding section
        tabContent = selectedTabVar.signal.map {
          case "chat"       => renderChatTab()
          case "linkViewer" => renderLinkViewerTab()
          case _            => renderChatTab()
        },
      ),
    )

  // ================================
  // Chat Section
  // ================================
  private def renderChatTab(): HtmlElement =
    val indexChangedObserver = Observer[String] {
      indexId =>
        if indexId != "None" then
          chatStatusVar.set(s"Selected index: $indexId")
        else
          chatStatusVar.set("Currently no index selected!")
    }

    div(
      cls := "chat-tab",

      // Index selector
      IndexSelector(
        availableIndexesSignal = availableIndexesVar.signal,
        selectedIndexVar = selectedIndexVar,
        onRefreshRequested = Observer(_ => indexEvents.writer.onNext(RefreshIndicesRequested)),
      ),

      // Listen to changes when the user selects a new index
      selectedIndexVar.signal --> indexChangedObserver,

      // Chat interface
      div(
        cls := "chat-interface-container",
        ChatInterface(
          messagesSignal = chatMessagesVar.signal, // Readonly variable
          onSendMessage = handleChatMessage,
        ),
      ),
      // This is optional
      // StatusBar(chatStatusVar.signal),
    )

  private def handleChatMessage(content: String): Unit =
    ChatInterface.handleNewMessage(
      content = content,
      chatMessagesVar = chatMessagesVar,
      selectedIndexVar = selectedIndexVar,
      chatStatusVar = chatStatusVar,
      contentService = contentService,
    )

  // ================================
  // Link Viewer Section
  // ================================
  private def renderLinkViewerTab(): HtmlElement =
    div(
      cls := "link-viewer-tab",
      LinkViewer(
        contentService = contentService,
        onIndexGenerated = Observer(
          indexName =>
            indexEvents.writer.onNext(IndexGenerated(indexName)),
        ),
      ),
    )
