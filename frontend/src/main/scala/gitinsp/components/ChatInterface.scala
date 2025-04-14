package gitinsp.components

import com.raquo.laminar.api.L.*
object ChatInterface:

  case class ChatMessage(id: String, isBot: Boolean, content: String)
  val inputVar = Var("")

  def apply(
    messagesSignal: Signal[Seq[ChatMessage]],
    onSendMessage: String => Unit,
  ): HtmlElement =
    div(
      cls := "chat-interface",
      div(
        div(
          cls := "chat-messages",
          // Readonly variable to display the messages
          children <-- messagesSignal.map {
            messages =>
              // Add all messages to the chat
              messages.map {
                message =>
                  div(
                    cls := (if message.isBot then "ai-message-wrapper"
                            else "user-message-wrapper "),
                    div(
                      div(
                        cls := "message-content",
                        message.content,
                      ),
                    ),
                  )
              }
          },
        ),
      ),
      div(
        cls := "chat-input-container",
        input(
          cls         := "chat-input",
          placeholder := "Type your message...",
          controlled(
            // This field allows a bidirectional binding between the input bar and the inputVar
            // https://laminar.dev/documentation#controlled-inputs
            value <-- inputVar.signal,
            onInput.mapToValue --> inputVar,
          ),
          // Send the message whenever the Enter key is pressed
          onKeyDown.filter(_.key == "Enter") --> (
            _ =>
              val message = inputVar.now().trim
              if message.nonEmpty then
                onSendMessage(message)
                inputVar.set("")
          ),
        ),
        button(
          cls := "send-button",
          "Send",
          // Send the message whenever the button is clicked
          onClick --> (
            _ =>
              val message = inputVar.now().trim
              if message.nonEmpty then
                onSendMessage(message)
                inputVar.set("")
          ),
        ),
      ),
    )
