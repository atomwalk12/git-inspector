package gitinsp

import com.raquo.laminar.api.L.*
import gitinsp.components.ChatInterface
import gitinsp.components.ChatInterface.ChatMessage
import gitinsp.components.TabContainer
import gitinsp.components.TabContainer.Tab
import org.scalajs.dom
object GitInspectorFrontend:
  // Labels
  val tabs = Seq(Tab("chat", "Chat"), Tab("linkViewer", "Link Viewer"))

  // Variuables allow read-write operations, while signals are read-only
  val selectedTabVar = Var("chat") // has also a set method
  val chatMessageVar = Var(Seq(
    ChatMessage(id = "welcome", isBot = true, content = "Hello, how can I help you?"),
  ))

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
          case "chat"       => renderChat()
          case "linkViewer" => renderLinkViewer()
          case _            => renderChat()
        },
      ),
    )

  private def renderChat(): HtmlElement =
    div(
      cls := "chat-tab",
      // Chat interface
      div(
        cls := "chat-interface-container",
        ChatInterface(
          messagesSignal = chatMessageVar.signal, // Readonly variable
          onSendMessage = handleNewMessage,
        ),
      ),
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
