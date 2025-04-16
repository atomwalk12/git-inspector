package gitinsp.api

import com.raquo.airstream.core.EventStream
import com.raquo.airstream.core.Observer
import com.raquo.airstream.eventbus.EventBus
import com.raquo.airstream.ownership.Owner
import gitinsp.models.StreamResponse
import io.circe.generic.auto.*
import io.circe.parser.*
import org.scalajs.dom.EventSource
import org.scalajs.dom.Headers
import org.scalajs.dom.HttpMethod
import org.scalajs.dom.MessageEvent
import org.scalajs.dom.RequestInit
import org.scalajs.dom.fetch

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.Dynamic.global as jsGlobal

class HttpClient(baseUrl: String):

  def get(endpoint: String, params: Map[String, String] = Map.empty): Future[String] =
    val url = s"$baseUrl/$endpoint"

    // Build query string from params
    val queryString = params.map {
      case (key, value) =>
        s"${encodeURIComponent(key)}=${encodeURIComponent(value)}"
    }.mkString("&")

    val fullUrl = s"$baseUrl/$endpoint${if params.nonEmpty then s"?$queryString" else ""}"

    fetch(fullUrl, new RequestInit { method = HttpMethod.GET })
      .toFuture
      .flatMap(
        response =>
          if !response.ok then
            jsGlobal.console.error(s"HTTP error for $endpoint: status ${response.status}")
            Future.failed(new Error(s"HTTP error for $endpoint: status ${response.status}"))
          else
            response.text().toFuture,
      )

  def post(endpoint: String, data: String): Future[String] =
    val url = s"$baseUrl/$endpoint"

    val _headers = new Headers()
    _headers.append("Content-Type", "application/x-www-form-urlencoded")

    val init = new RequestInit {
      method = HttpMethod.POST
      body = data
      headers = _headers
    }

    fetch(url, init)
      .toFuture
      .flatMap(
        response =>
          if !response.ok then
            jsGlobal.console.error(s"HTTP error for $endpoint: status ${response.status}")
            Future.failed(new Error(s"HTTP error for $endpoint: status ${response.status}"))
          else
            response.text().toFuture,
      )

  private def encodeURIComponent(s: String): String =
    js.URIUtils.encodeURIComponent(s)

  def streamChat(endpoint: String, data: Map[String, String]): EventStream[String] =
    // The event stream is used to accumulate data while it is being sent
    // The writer allows to the caller to listen to the stream
    val bus    = new EventBus[String]
    val writer = bus.writer

    // The encodeURIComponent is necessary for the POST request
    val queryString = data.map {
      case (key, value) =>
        s"${encodeURIComponent(key)}=${encodeURIComponent(value)}"
    }.mkString("&")

    val url = s"$baseUrl/$endpoint?$queryString"

    // This is provided by Scala.js to handle SSE
    val eventSource = new EventSource(url)

    // The callback to listen to messages
    eventSource.onmessage = (event: MessageEvent) =>
      // All messages are within the data property (the SSE spec)
      val rawData = event.data.toString

      // Parse the responses
      parse(rawData).flatMap(_.as[StreamResponse]) match
        case Right(response) =>
          writer.onNext(response.text) // the text field is part of the backend response
        case Left(error) =>
          writer.onNext(rawData)

    // In case errors occur
    eventSource.onerror = (_: org.scalajs.dom.Event) =>
      eventSource.close()
      writer.onError(new Exception("EventSource error"))

    // To ensure that the stream is closed
    // See https://laminar.dev/documentation#laminars-use-of-airstream-ownership
    val owner = new Owner {
      override def killSubscriptions(): Unit = {
        super.killSubscriptions()
        eventSource.close()
      }
    }

    // This ensures the EventSource is closed when there are no more observers of the stream
    val events = bus.events
    events.addObserver(Observer.empty)(owner)

    events
