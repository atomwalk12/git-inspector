package gitinsp.components
import com.raquo.laminar.api.L.*
import gitinsp.models.ChatMessage
import gitinsp.models.ChatSession
import gitinsp.services.ContentService
import gitinsp.util.IDGenerator
import org.scalajs.dom
object ChatInterface {

  def handleNewMessage(
    content: String,
    chatSessionVar: Var[ChatSession],
    selectedIndexVar: Var[String],
    chatStatusVar: Var[String],
    contentService: ContentService,
  ): Unit =
    // Add user message
    val userMsgId = IDGenerator.generateId("user")
    chatSessionVar.update(
      msgs =>
        msgs :+ ChatMessage(userMsgId, false, content),
    )

    // Add an initial bot message that will be updated as streaming progresses
    val botMsgId          = IDGenerator.generateId("bot")
    val initialBotMessage = ChatMessage(botMsgId, true, "...")
    chatSessionVar.update(msgs => msgs :+ initialBotMessage)

    // Get the current index name
    val indexName = selectedIndexVar.now()

    // Start streaming
    contentService.chat(content, indexName)
      .foreach {
        streamedContent =>
          // Update the bot message with the latest content
          chatSessionVar.update {
            messages =>
              messages.map {
                msg =>
                  if msg.id == botMsgId then {
                    msg.copy(content = streamedContent)
                  }
                  else {
                    msg
                  }
              }
          }

          // Update status
          indexName match
            case "" =>
              chatStatusVar.set("No index selected")
            case _ =>
              chatStatusVar.set(s"Currently chatting with $indexName...")

        // The unsafe window owner is a legitimate use in this case (use doc comment)
        // The owner never kills its possessions because the observer is global and
        // persists throughout the lifetime of the application
      }(unsafeWindowOwner)

  def apply(
    messagesSignal: Signal[ChatSession],
    onSendMessage: String => Unit,
  ): HtmlElement =
    val inputVar       = Var("")
    val messagesDivRef = Var[Option[dom.html.Div]](None)

    div(
      cls := "chat-interface",
      cls := "content-display",
      div(
        cls := "chat-messages",
        onMountCallback(ctx => messagesDivRef.set(Some(ctx.thisNode.ref))),

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

      // Scroll to the bottom of the messages div when the messages change
      messagesSignal --> {
        _ =>
          dom.window.setTimeout(
            () => {
              messagesDivRef.now().foreach {
                messagesDiv => messagesDiv.scrollTop = messagesDiv.scrollHeight
              }
            },
            50,
          )
      },
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
