package gitinsp.domain.models

import java.util.Locale

/** Represents a URL with validation and conversion capabilities */
final case class URL(val value: String):
  override def toString: String = value

  /** Converts this URL to an AIServiceURL
    * @return The converted AIServiceURL
    */
  def toAIServiceURL(): AIServiceURL =
    if value == "default" then
      AIServiceURL.default
    else
      val desanitized = value
        .replaceAll("^https?://", "")
        .replace("/", "[slash]")
      AIServiceURL(desanitized)

  /** Converts this URL to a QdrantURL with the specified category
    * @param category The category to assign to the QdrantURL
    * @return The converted QdrantURL
    */
  def toQdrantURL(category: Category): QdrantURL =
    toAIServiceURL().toQdrantURL(category)

/** Companion object for URL with default URL and factory method */
object URL:
  /** Default URL instance */
  val default = new URL("default")

  /** Creates a new URL instance with validation
    * @param value The URL string to validate and wrap
    * @return A new URL instance
    * @throws AssertionError if the URL format is invalid
    */
  def apply(value: String): URL =
    // Ensure that the value is an URL
    if value.isEmpty() || value == "default" || value.toLowerCase(Locale.ROOT) == "none" then
      default
    else
      val regex = "^(https?://)?([\\w-]+\\.)+[\\w-]+(/[\\w-./?%&=]*)?$".r
      assert(regex.matches(value), s"Invalid GitHub URL format: $value")
      new URL(value)

/** Represents a URL for AI services with sanitized format */
final case class AIServiceURL(val value: String):
  if value != "default" then
    assert(value.contains("[slash]"), s"Value must contain a slash: $value")

  /** Converts this AIServiceURL back to a regular URL
    * @return The desanitized URL
    */
  def toURL(): URL =
    val desanitized = value
      .replace("[slash]", "/")
      .replace("[colon]", ":")
    URL(desanitized)

  /** Converts this AIServiceURL to a QdrantURL with the specified category
    * @param category The category to assign to the QdrantURL
    * @return The converted QdrantURL
    */
  def toQdrantURL(category: Category): QdrantURL =
    QdrantURL(s"$value-${category}", category)

  override def toString: String = value

/** Companion object for AIServiceURL with default instance */
object AIServiceURL:

  val default = AIServiceURL("default")

/** Represents a URL for Qdrant vector database with category information */
final case class QdrantURL(val value: String, val category: Category):
  val isCode = value.endsWith(s"-${Category.CODE}")
  val isText = value.endsWith(s"-${Category.TEXT}")

  assert(isCode || isText, s"Value must end with a dash and language: $value")

  /** Builds the AIServiceURL from this QdrantURL by removing the category suffix
    * @return The derived AIServiceURL
    */
  def buildAIServiceURL(): AIServiceURL =
    AIServiceURL(value.lastIndexOf("-") match
      case idx if idx > 0 => value.substring(0, idx)
      case _              => value)

  override def toString: String = value

/** Companion object for QdrantURL with factory method */
object QdrantURL:
  /** Creates a new QdrantURL with category detection from the value
    * @param value The URL string with category suffix
    * @return A new QdrantURL instance with detected category
    * @throws AssertionError if the value doesn't end with a valid category suffix
    */
  def apply(value: String): QdrantURL =
    val isCode = value.endsWith(s"-${Category.CODE}")
    val isText = value.endsWith(s"-${Category.TEXT}")
    assert(isCode || isText, s"Value must end with a dash and language: $value")
    QdrantURL(value, if isCode then Category.CODE else Category.TEXT)
