package gitinsp.components
import com.raquo.laminar.api.L.*

object ChatInterface {

  case class ChatMessage(id: String, isBot: Boolean, content: String)

  def apply(
    messagesSignal: Signal[Seq[ChatMessage]],
    onSendMessage: String => Unit,
  ): HtmlElement =
    val inputVar = Var("")

    div(
      cls := "chat-interface",
      cls := "content-display",
      div(
        cls := "chat-messages",
        // Readonly variable to display the messages
        children <-- messagesSignal.map {
          messages =>
            // Add all messages to the chat
            messages.map {
              msg =>
                div(
                  cls := "message-wrapper",
                  cls := (if msg.isBot then "ai-message-wrapper" else "user-message-wrapper"),
                  div(
                    cls := "message-bubble",
                    cls := (if msg.isBot then "ai-message" else "user-message"),
                    div(cls := "message-content", msg.content),
                  ),
                )
            }
        },
      ),
      div(
        cls := "chat-input-container",
        input(
          cls         := "chat-input",
          placeholder := "Type a message...",
          controlled(
            // This field allows a bidirectional binding between the input bar and the inputVar
            // https://laminar.dev/documentation#controlled-inputs
            value <-- inputVar.signal,
            onInput.mapToValue --> inputVar,
          ),
          // Send the message whenever the Enter key is pressed
          onKeyDown.filter(_.key == "Enter") --> {
            _ =>
              val message = inputVar.now().trim
              if message.nonEmpty then
                onSendMessage(message)
                inputVar.set("")
          },
        ),
        button(
          cls := "send-button",
          // Send the message whenever the button is clicked
          onClick --> {
            _ =>
              val message = inputVar.now().trim
              if message.nonEmpty then
                onSendMessage(message)
                inputVar.set("")
          },
        ),
      ),
    )
}
