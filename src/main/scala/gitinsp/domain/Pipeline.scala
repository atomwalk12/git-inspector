package gitinsp.domain

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import gitinsp.infrastructure.CacheService

import scala.concurrent.ExecutionContext

object Pipeline:
  def apply(using s: ActorSystem, m: Materializer, e: ExecutionContext): Pipeline =
    new PipelineImpl(ChatService(), CacheService())

  def apply(chat: ChatService, cache: CacheService)(using
    ActorSystem,
    Materializer,
    ExecutionContext,
  ): Pipeline =
    new PipelineImpl(chat, cache)

  private class PipelineImpl(
    val chatService: ChatService,
    val cacheService: CacheService,
  )(using s: ActorSystem, m: Materializer, e: ExecutionContext) extends Pipeline:

    def chat(message: String, idxName: String): Source[String, NotUsed] =
      val aiservice = cacheService.getAIService(idxName)
      chatService.chat(message, aiservice)

trait Pipeline:
  def chat(message: String, idxName: String): Source[String, NotUsed]
