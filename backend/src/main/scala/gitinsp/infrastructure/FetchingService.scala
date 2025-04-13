package gitinsp.infrastructure

import com.typesafe.scalalogging.LazyLogging
import gitinsp.domain.interfaces.infrastructure.FetchingService

import java.net.HttpURLConnection

import scala.io.Source
import scala.util.Failure
import scala.util.Try

object FetchingService extends LazyLogging:
  def apply(): FetchingService =
    new URLClientImpl()

  // This class is not absolutely necessary, but it's useful to streamline tests that rely on
  // fetching external data (since we have a private method).
  private class URLClientImpl extends FetchingService:
    def fetchUrl(
      url: String,
      connectTimeout: Int,
      readTimeout: Int,
      requestMethod: String,
      headers: Map[String, String],
    ): Try[String] =
      import scala.util.Using
      import java.net.URI

      val conn = URI(url).toURL.openConnection()

      conn match
        case httpConn: HttpURLConnection =>
          Try {
            // Configure connection
            httpConn.setConnectTimeout(connectTimeout)
            httpConn.setReadTimeout(readTimeout)
            httpConn.setRequestMethod(requestMethod)

            // Add headers
            headers.foreach((key, value) => httpConn.setRequestProperty(key, value))

            // Get response
            Using.resource(httpConn.getInputStream) {
              stream => Source.fromInputStream(stream, "UTF-8").mkString
            }
          }.recoverWith {
            case e: Exception =>
              logger.error(s"Error fetching URL: $url")
              Failure(e)
          }
        case _ =>
          val e = new IllegalArgumentException(s"Expected HTTP connection for URL: $url")
          Failure(e)
