package gitinsp.domain.interfaces.infrastructure

import scala.util.Try

trait FetchingService:
  def fetchUrl(
    url: String,
    connectTimeout: Int,
    readTimeout: Int,
    requestMethod: String,
    headers: Map[String, String],
  ): Try[String]
