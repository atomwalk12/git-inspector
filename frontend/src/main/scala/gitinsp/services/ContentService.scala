package gitinsp.services

import com.raquo.airstream.core.Signal
import gitinsp.api.HttpClient
import gitinsp.components.LinkViewer.statusVar
import gitinsp.models.FetchContentRequest
import gitinsp.models.FetchIndexesResponse
import gitinsp.models.GenerateIndex
import gitinsp.models.GenerateIndexResponse
import gitinsp.models.IndexOption
import gitinsp.models.RemoveIndex
import gitinsp.models.RemoveIndexResponse
import upickle.default.ReadWriter as RW
import upickle.default.macroRW
import upickle.default.read
import upickle.default.write

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js.URIUtils.encodeURIComponent
import scala.util.Try

class ContentService(httpClient: HttpClient):

  // Define implicit readers/writers for model classes
  implicit val fetchContentRequest: RW[FetchContentRequest]       = macroRW
  implicit val generateIndexRW: RW[GenerateIndex]                 = macroRW
  implicit val generateIndexResponseRW: RW[GenerateIndexResponse] = macroRW
  implicit val removeIndexRW: RW[RemoveIndex]                     = macroRW
  implicit val removeIndexResponseRW: RW[RemoveIndexResponse]     = macroRW

  def fetchContent(url: String, format: String, extension: String): Future[String] =
    val params = Map(
      "link"      -> url,
      "format"    -> format,
      "extension" -> extension,
    )
    httpClient.get("fetch", params)
      .recover {
        case e: Exception =>
          statusVar.set(s"Error fetching content: ${e.getMessage}")
          "Error fetching content. Please try again."
      }

  def generateIndex(
    content: GenerateIndex,
    options: Map[String, String],
  ): Future[GenerateIndexResponse] =
    val json     = write[GenerateIndex](content)
    val contents = s"data=${encodeURIComponent(json)}"
    httpClient.post("generate", contents)
      .map {
        responseText =>
          implicit val generateIndexResponseRW: RW[GenerateIndexResponse] = macroRW
          read[GenerateIndexResponse](responseText)
      }
      .recoverWith {
        case e: Exception =>
          Future.failed(e)
      }

  def removeIndex(indexName: String): Future[Option[String]] =
    val json     = write[RemoveIndex](RemoveIndex(indexName))
    val contents = s"data=${encodeURIComponent(json)}"

    httpClient.post("remove", contents)
      .map {
        responseText =>
          val result = read[RemoveIndexResponse](responseText)
          Some(result.result)
      }
      .recover {
        case e: Exception =>
          statusVar.set(s"Error removing index: ${e.getMessage}")
          None
      }

  def fetchAvailableIndices(): Future[Option[Seq[IndexOption]]] =
    httpClient.get("list_indexes")
      .map {
        response =>
          Try {
            implicit val indicesResponseRW: RW[FetchIndexesResponse] = macroRW
            val parsed = read[FetchIndexesResponse](response)
            Some(List(IndexOption.default) ++ parsed.indexes.map(i => IndexOption(i, i)))
          }.getOrElse {
            statusVar.set(s"Error parsing indices response")
            None
          }
      }
      .recover {
        case e: Exception =>
          statusVar.set(s"Error fetching indices: ${e.getMessage}")
          None
      }

  def chat(
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
