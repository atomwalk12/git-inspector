package gitinsp.domain.models

import akka.NotUsed
import akka.stream.scaladsl.Source
import com.typesafe.scalalogging.LazyLogging
import dev.langchain4j.data.document.Document
import dev.langchain4j.data.document.Metadata
import dev.langchain4j.rag.content.Content

import java.util.Map as JMap
// TODO: Refactor this
// ============================
// Language
// ============================

/** Enum for supported programming languages.
  * Each case includes a short string representation (`value`) often used as a file extension.
  */
enum Language(val value: String):
  case CPP        extends Language("cpp")
  case GO         extends Language("go")
  case JAVA       extends Language("java")
  case KOTLIN     extends Language("kt")
  case JS         extends Language("js")
  case TS         extends Language("ts")
  case PHP        extends Language("php")
  case PROTO      extends Language("proto")
  case PYTHON     extends Language("py")
  case RST        extends Language("rst")
  case RUBY       extends Language("rb")
  case RUST       extends Language("rs")
  case SCALA      extends Language("scala")
  case SWIFT      extends Language("swift")
  case MARKDOWN   extends Language("md")
  case LATEX      extends Language("latex")
  case HTML       extends Language("html")
  case SOL        extends Language("sol")
  case CSHARP     extends Language("cs")
  case COBOL      extends Language("cob")
  case C          extends Language("c")
  case LUA        extends Language("lua")
  case HASKELL    extends Language("hs")
  case ELIXIR     extends Language("ex")
  case POWERSHELL extends Language("ps")

  // Override toString to return the custom value
  override def toString: String = value

  def category: Category =
    this match
      case Language.MARKDOWN => Category.TEXT
      case _                 => Category.CODE

enum Category(val value: String):
  case TEXT extends Category("text")
  case CODE extends Category("code")

  override def toString: String = value

// ============================
// RepositoryWithLanguages
// ============================

object RepositoryWithLanguages extends LazyLogging:
  /** Trims the part after the last dash in a string
    *  For example: "repo1-text" becomes "repo1"
    */
  def detectLanguage(language: String): Option[List[Language]] =
    Option(language).map(_.trim).flatMap {
      case lang if lang.contains(",") =>
        Some(
          lang.split(",")
            .toList
            .flatMap(l => findLanguage(l.trim)),
        )
      case lang if lang.nonEmpty =>
        findLanguage(lang).map(List(_))
      case _ =>
        Some(List())
    }.orElse(Some(List()))

  def from(qdrantCollections: List[QdrantURL]): List[RepositoryWithCategories] =
    // Extract repository names by removing language suffix
    val repoGroups = qdrantCollections.groupBy(_.buildAIServiceURL())

    // Create one GitRepository per unique repository with all its languages
    repoGroups.map {
      case (baseName, indices) =>
        val categories = indices.map(_.category).distinct
        RepositoryWithCategories(baseName.toURL(), categories, List.empty)
    }.toList

  private def findLanguage(lang: String): Option[Language] =
    Language.values.find(_.toString == lang) match
      case Some(language) => Some(language)
      case None =>
        val message = s"Unsupported language: $lang"
        logger.error(message)
        None

  def detectLanguages(languages: String): List[Language] =
    languages.split(",")
      .toList
      .flatMap(l => findLanguage(l.trim))
      .distinctBy(_.toString)

  def detectLanguageFromFile(filePath: String): Option[Language] =
    val language = filePath.split("\\.").last
    findLanguage(language)

final case class RepositoryWithCategories(
  url: URL,
  categories: List[Category],
  docs: List[CodeFile],
)

final case class RepositoryWithLanguages(url: URL, languages: List[Language], docs: List[CodeFile]):
  val indexNames = languages.map(lang => url.toAIServiceURL().toQdrantURL(lang.category))
  assert(indexNames.length == languages.length, s"Length mismatch: $indexNames, $languages")

  override def toString: String = s"GitRepository(url=$url, languages=$languages, docs=$docs)"

  def toRepositoryWithCategories(): RepositoryWithCategories =
    RepositoryWithCategories(url, languages.map(_.category).distinct, docs)

// ============================
// URL
// ============================

final case class URL(val value: String):
  override def toString: String = value

  def toAIServiceURL(): AIServiceURL =
    val desanitized = value
      .replaceAll("^https?://", "")
      .replace("/", "[slash]")
    AIServiceURL(desanitized)

  def toQdrantURL(category: Category): QdrantURL =
    toAIServiceURL().toQdrantURL(category)

object URL:
  def apply(value: String): URL =
    // ENsure that the value is an URL
    val regex = "^(https?://)?([\\w-]+\\.)+[\\w-]+(/[\\w-./?%&=]*)?$".r
    assert(regex.matches(value), s"Invalid GitHub URL format: $value")
    new URL(value)

// ============================
// AIServiceURL
// ============================

object AIServiceURL:
  val default = new AIServiceURL("default")

final case class AIServiceURL(val value: String):
  if value != "default" then
    assert(value.contains("[slash]"), s"Value must contain a slash: $value")

  def toURL(): URL =
    val desanitized = value
      .replace("[slash]", "/")
      .replace("[colon]", ":")
    URL(desanitized)

  def toQdrantURL(category: Category): QdrantURL =
    QdrantURL(s"$value-${category}", category)

  override def toString: String = value

// ============================
// QdrantURL
// ============================

final case class QdrantURL(val value: String, val category: Category):
  val isCode = value.endsWith(s"-${Category.CODE}")
  val isText = value.endsWith(s"-${Category.TEXT}")
  assert(isCode || isText, s"Value must end with a dash and language: $value")
  def buildAIServiceURL(): AIServiceURL =
    AIServiceURL(value.lastIndexOf("-") match
      case idx if idx > 0 => value.substring(0, idx)
      case _              => value)

  override def toString: String = value

object QdrantURL:
  def apply(value: String): QdrantURL =
    val isCode = value.endsWith(s"-${Category.CODE}")
    val isText = value.endsWith(s"-${Category.TEXT}")
    assert(isCode || isText, s"Value must end with a dash and language: $value")
    QdrantURL(value, if isCode then Category.CODE else Category.TEXT)

// ============================
// CodeFile
// ============================

final case class CodeFile(content: String, language: Language, path: String):
  def createLangchainDocument(): Option[Document] =
    Option(content.trim)
      .filter(_.nonEmpty)
      .map(
        trimmedContent => {
          val metadata = Metadata.from(JMap.of("file_name", path, "code", language.toString))
          Document.from(trimmedContent, metadata)
        },
      )

type TextSegment      = Content
type SearchResults    = TextSegment
type StreamedResponse = Source[String, NotUsed]
