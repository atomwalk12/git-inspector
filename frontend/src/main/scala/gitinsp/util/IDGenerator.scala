package gitinsp.util

import java.util.UUID

/** Simple ID generator for Scala.js that doesn't rely on java.util.UUID */
object IDGenerator {
  private var counter = 0

  /** Generates a unique ID with a prefix */
  def generateId(prefix: String): String = {
    UUID.randomUUID().toString
  }
}
