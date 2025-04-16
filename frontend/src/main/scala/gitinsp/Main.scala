package gitinsp

import com.raquo.laminar.api.L.*
import gitinsp.api.HttpClient
import gitinsp.components.ChatInterface
import gitinsp.components.ChatInterface.ChatMessage
import gitinsp.components.IndexSelector
import gitinsp.components.LinkViewer
import gitinsp.components.StatusBar
import gitinsp.components.TabContainer
import gitinsp.components.TabContainer.Tab
import gitinsp.models.IndexEvent
import gitinsp.models.IndexGenerated
import gitinsp.models.IndexOption
import gitinsp.models.RefreshIndicesRequested
import gitinsp.models.RemoveIndexRequested
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
    IndexOption.default,
  ))
  private val selectedIndexVar: Var[String] = Var(IndexOption.default.id)
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
      case RemoveIndexRequested(name) =>
        removeIndex(name)
        selectedIndexVar.set(IndexOption.default.id)
        chatMessagesVar.update(messages => messages.filter(_.id == "welcome"))
    }(unsafeWindowOwner)

  private def removeIndex(name: String): Unit =
    chatStatusVar.set(s"Removing index: $name")
    contentService
      .removeIndex(name)
      .foreach {
        case Some(result) =>
          chatStatusVar.set(s"Index removed: $result")
          refreshAvailableIndices()
        case None =>
          chatStatusVar.set("Failed to remove index. Ensure the backend is running.")
      }(global)

  private def refreshAvailableIndices(): Unit =
    chatStatusVar.set("Refreshing available indices...")
    contentService
      .fetchAvailableIndices()
      .map {
        case Some(indices) =>
          availableIndexesVar.set(indices)
          if indices.size == 1 then
            chatStatusVar.set("No indices found")
          else
            chatStatusVar.set(s"Found ${indices.size - 1} indices")
        case None =>
          chatStatusVar.set("Failed to refresh indices. Ensure the backend is running.")
      }(global)

  // ================================
  // UI Components
  // ================================

  // Cache tab components to avoid recreating them on each tab switch
  private val chatTabElement: HtmlElement       = createChatTab()
  private val linkViewerTabElement: HtmlElement = createLinkViewerTab()

  private def appElement(): HtmlElement =
    div(
      cls := "app-container",
      TabContainer(
        tabs = tabs,
        selectedTabVar = selectedTabVar,

        // When a new tab is selected, show the cached tab element
        tabContent = selectedTabVar.signal.map {
          case "chat"       => chatTabElement
          case "linkViewer" => linkViewerTabElement
          case _            => chatTabElement
        },
      ),
    )

  // ================================
  // Chat Section
  // ================================
  private def createChatTab(): HtmlElement =
    val indexChangedObserver = Observer[String] {
      indexId =>
        if indexId != IndexOption.default.id then
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
        onRemoveRequested =
          Observer(_ => indexEvents.writer.onNext(RemoveIndexRequested(selectedIndexVar.now()))),
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
      StatusBar(chatStatusVar.signal),
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
  private def createLinkViewerTab(): HtmlElement =
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
