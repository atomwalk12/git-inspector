package gitinsp.utils

import dev.langchain4j.data.document.Document
import dev.langchain4j.data.document.Metadata

import java.util.Map as JMap

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
  case CODE       extends Language("code") // Placeholder for all code languages
  case RST        extends Language("rst")
  case RUBY       extends Language("rb")
  case RUST       extends Language("rs")
  case SCALA      extends Language("scala")
  case SWIFT      extends Language("swift")
  case MARKDOWN   extends Language("md")
  case TEXT       extends Language("text")
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

  def category: String =
    this match
      case Language.MARKDOWN => Language.TEXT.value
      case _                 => Language.CODE.value

object GitRepository:
  def detectLanguage(language: String): Either[Language, List[Language]] =
    Option(language).map(_.trim).flatMap {
      case lang if lang.contains(",") =>
        Some(Right(
          lang.split(",")
            .toList
            .map(l => findLanguage(l.trim)),
        ))
      case lang if lang.nonEmpty =>
        Some(Left(findLanguage(lang)))
      case _ =>
        Some(Right(List()))
    }.getOrElse(Right(List()))

  private def findLanguage(lang: String): Language =
    Language.values.find(_.toString == lang).getOrElse(Language.CODE)

  def detectLanguages(languages: String): List[Language] =
    languages.split(",")
      .toList
      .map(l => findLanguage(l.trim))

final case class GitRepository(url: String, languages: List[Language], docs: List[GitDocument]):
  val indexNames: List[IndexName] = languages.map(indexName)
  assert(indexNames.length == languages.length, s"Length mismatch: $indexNames, $languages")

  def indexName(lang: Language): IndexName =
    val sanitizedName = URLSanitizerService.sanitize(url)
    // Here we are creating different separate index for each code language
    // Since textual input is generally markdown, we are creating a single, separate index for it
    // The reason is that the document splitter must use differet separators according to the
    // language being used. By creating separate indexes, we ensure that this is done correctly.
    // The markdown index contains all the markdown text, and only one list of separators is needed.
    lang match
      case Language.MARKDOWN => IndexName(s"$sanitizedName-${Language.TEXT.toString}", lang)
      case lang              => IndexName(s"$sanitizedName-${lang.toString}", lang)

  override def toString: String = s"GitRepository(url=$url, languages=$languages, docs=$docs)"

final case class IndexName(name: String, language: Language)

final case class GitDocument(content: String, language: Language, path: String):
  def createLangchainDocument(): Option[Document] =
    Option(content.trim)
      .filter(_.nonEmpty)
      .map(
        trimmedContent => {
          val metadata = Metadata.from(JMap.of("file_name", path, "code", language.toString))
          Document.from(trimmedContent, metadata)
        },
      )
