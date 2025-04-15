package gitinsp.services

import com.raquo.airstream.core.Signal
import gitinsp.api.HttpClient
import gitinsp.components.IndexOption

import scala.concurrent.Future

class ContentService(httpClient: HttpClient):

  def chatStreaming(
    message: String,
    indexName: String,
  ): Signal[String] =

    // Prepare data for request
    val data = Map(
      "msg"       -> message,
      "indexName" -> indexName,
    )

    // Make streaming request
    httpClient.streamChat("chat", data)
      .scanLeft("") { (accumulated, newChunk) => accumulated + processChunk(newChunk) }

  private def processChunk(chunk: String): String = chunk match
    case c if c.startsWith("<think>")  => "🤔\n"
    case c if c.startsWith("</think>") => "\n🤔\n"
    case c if c.isEmpty                => "\n"
    case c                             => c

  def fetchAvailableIndices(): Future[Seq[IndexOption]] = ???
