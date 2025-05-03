package gitinsp.domain.interfaces.infrastructure

import scala.util.Try

/** Service for handling HTTP communication with external resources
  * Provides a generic interface for fetching content from URLs with configurable parameters
  */
trait FetchingService:
  /** Fetches content from a specified URL with custom HTTP settings
    * @param url The URL to fetch content from
    * @param connectTimeout Maximum time to wait for connection in milliseconds
    * @param readTimeout Maximum time to wait for data in milliseconds
    * @param requestMethod HTTP method to use (GET, POST, etc.)
    * @param headers Map of HTTP headers to include in the request
    * @return The content of the URL response as a string wrapped in a Try
    */
  def fetchUrl(
    url: String,
    connectTimeout: Int,
    readTimeout: Int,
    requestMethod: String,
    headers: Map[String, String],
  ): Try[String]
