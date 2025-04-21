package gitinsp.domain
import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.Source
import dev.langchain4j.rag.content.Content
import gitinsp.domain.interfaces.application.ChatService
import gitinsp.domain.interfaces.infrastructure.ContentService
import gitinsp.domain.models.Assistant
import gitinsp.domain.models.SearchResult
import gitinsp.domain.models.StreamedResponse

import java.util.List

import scala.concurrent.ExecutionContext
import scala.jdk.CollectionConverters.ListHasAsScala

object ChatService:
  type AS = ActorSystem
  type M  = Materializer
  type EC = ExecutionContext

  def apply(prettyFmt: Boolean, contentService: ContentService)(using AS, M, EC): ChatService =
    new ChatServiceImpl(prettyFmt, contentService)

  private class ChatServiceImpl(pretty: Boolean, val fmt: ContentService)(using AS, M, EC)
      extends ChatService:

    override def chat(msg: String, ai: Assistant): StreamedResponse =
      streamChat(msg, ai)

    def streamChat(msg: String, ai: Assistant): StreamedResponse =
      val (q, src) = Source
        .queue[String](100, OverflowStrategy.backpressure)
        .preMaterialize()

      ai.chat(msg)
        .onRetrieved {
          // This is triggered when data is retrieved from the index
          (resp: List[SearchResult]) =>
            resp.asScala.toList.zipWithIndex.foreach {
              (content, idx) =>
                val text      = content.textSegment().text()
                val tmpl      = fmt.docTemplate(idx + 1, text)
                val formatted = if pretty then fmt.toHtml(tmpl) else fmt.toPlainText(tmpl)
                q.offer(formatted)
            }
        }
        .onPartialResponse(r => q.offer(r))
        .onCompleteResponse(_ => q.complete())
        .onError(q.fail)
        .start()

      src
