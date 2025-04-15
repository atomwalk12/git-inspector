package gitinsp.api

import com.raquo.airstream.core.EventStream
import com.raquo.airstream.core.Observer
import com.raquo.airstream.eventbus.EventBus
import com.raquo.airstream.ownership.Owner
import gitinsp.models.StreamResponse
import io.circe.generic.auto.*
import io.circe.parser.*
import org.scalajs.dom.EventSource
import org.scalajs.dom.MessageEvent

import scala.scalajs.js

class HttpClient(baseUrl: String):

  private def encodeURIComponent(s: String): String =
    js.URIUtils.encodeURIComponent(s)

  // Add PUT, DELETE, etc. as needed

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
