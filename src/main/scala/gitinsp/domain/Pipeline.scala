package gitinsp.domain

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import gitinsp.infrastructure.CacheService
import gitinsp.utils.GitRepository

import scala.concurrent.ExecutionContext

object Pipeline:
  def apply(using s: ActorSystem, m: Materializer, e: ExecutionContext): Pipeline =
    new PipelineImpl(ChatService(), CacheService(), IngestorService())

  def apply(chat: ChatService, cache: CacheService, ingestor: IngestorService)(using
    ActorSystem,
    Materializer,
    ExecutionContext,
  ): Pipeline =
    new PipelineImpl(chat, cache, ingestor)

  private class PipelineImpl(
    val chatService: ChatService,
    val cacheService: CacheService,
    val ingestorService: IngestorService,
  )(using s: ActorSystem, m: Materializer, e: ExecutionContext) extends Pipeline:

    def chat(message: String, idxName: String): Source[String, NotUsed] =
      val aiservice = cacheService.getAIService(idxName)
      chatService.chat(message, aiservice)

    def generateIndex(repository: GitRepository): Unit =
      ingestorService.ingest(repository)

trait Pipeline:
  def chat(message: String, idxName: String): Source[String, NotUsed]
  def generateIndex(repository: GitRepository): Unit
