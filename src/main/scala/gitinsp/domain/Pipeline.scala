package gitinsp.domain

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import gitinsp.infrastructure.CacheService
import gitinsp.utils.GitRepository
import gitinsp.utils.IndexName

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

    def chat(message: String, index: Option[IndexName]): Source[String, NotUsed] =
      index match
        case Some(idx) =>
          val aiservice = cacheService.getAIService(Some(idx))
          chatService.chat(message, aiservice)
        case None =>
          chatService.chat(message, cacheService.getAIService(None))

    def generateIndex(repository: GitRepository, regenerate: Boolean): Unit =
      // Delete current repository if it exists
      if regenerate then ingestorService.deleteRepository(repository)

      // Ingest the new repository
      ingestorService.ingest(repository)

    def regenerateIndex(repository: GitRepository): Unit =
      // Delete current repository if it exists
      generateIndex(repository, true)

trait Pipeline:
  def chat(message: String, index: Option[IndexName]): Source[String, NotUsed]
  def generateIndex(repository: GitRepository, regenerate: Boolean): Unit
  def regenerateIndex(repository: GitRepository): Unit
