package gitinsp

import com.raquo.laminar.api.L.*
import gitinsp.components.ChatInterface
import gitinsp.components.ChatInterface.ChatMessage
import gitinsp.components.IndexOption
import gitinsp.components.IndexSelector
import gitinsp.components.StatusBar
import gitinsp.components.TabContainer
import gitinsp.components.TabContainer.Tab
import org.scalajs.dom
import utils.IndexEvent
import utils.RefreshIndicesRequested

object GitInspectorFrontend:
  // Labels
  val tabs = Seq(Tab("chat", "Chat"), Tab("linkViewer", "Link Viewer"))

  // The shared variables are managed from the parent as opposed to the child
  // This makes it easier to share the state between the components
  val availableIndexesVar = Var(Seq(
    IndexOption("None", "None"),
    IndexOption("index1", "Index 1"),
  ))

  // Variuables allow read-write operations, while signals are read-only
  val selectedTabVar = Var("chat") // has also a set method
  val chatMessageVar = Var(Seq(
    ChatMessage(id = "welcome", isBot = true, content = "Hello, how can I help you?"),
  ))
  val selectedIndexVar = Var("None")
  val chatStatusVar    = Var("Welcome to Git Inspector!")

  // Event bus to listen to events from the index selector
  val indexEvents = new EventBus[IndexEvent]()

  def main(args: Array[String]): Unit =
    // Render the app element when the content is loaded
    renderOnDomContentLoaded(dom.document.querySelector("#app"), appElement())

  private def appElement(): HtmlElement =
    div(
      cls := "app-container",
      TabContainer(
        tabs = tabs,
        selectedTabVar = selectedTabVar,

        // on selected tab change, change the content of the section
        tabContent = selectedTabVar.signal.map {
          case "chat"       => renderChatTab()
          case "linkViewer" => renderLinkViewer()
          case _            => renderChatTab()
        },
      ),
    )

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
          messagesSignal = chatMessageVar.signal, // Readonly variable
          onSendMessage = handleNewMessage,
        ),
      ),
      StatusBar(chatStatusVar.signal),
    )

  private def handleNewMessage(message: String): Unit =
    // This happens when the user sends the send button in the chat interface
    dom.console.log(s"New message: $message")
    chatMessageVar.update(
      messages =>
        messages :+ ChatMessage(id = "user", isBot = false, content = message),
    )

  private def renderLinkViewer(): HtmlElement =
    div("Link Viewer")
