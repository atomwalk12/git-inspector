package gitinsp.util

import scala.scalajs.js.Date

/** Simple ID generator for Scala.js that doesn't rely on java.util.UUID */
object IDGenerator {
  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private var counter = 0

  /** Generates a unique ID with a prefix */
  def generateId(prefix: String): String = {
    counter += 1
    val timestamp = new Date().getTime().toLong
    s"$prefix-$timestamp-$counter"
    // This doesn't compile: UUID.randomUUID().toString
  }
}
