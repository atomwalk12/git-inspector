package gitinsp.domain.models

import com.typesafe.scalalogging.LazyLogging
import dev.langchain4j.data.document.Document
import dev.langchain4j.data.document.Metadata

import java.util.Map as JMap

/** Represents a repository with its categories and code files */
final case class RepositoryWithCategories(
  url: URL,
  categories: List[Category],
  docs: List[CodeFile],
)

/** Represents a Git repository with its languages and code files */
final case class GitRepository(url: URL, languages: List[Language], docs: List[CodeFile]):

  val indexNames = languages.map(lang => url.toAIServiceURL().toQdrantURL(lang.category))
  assert(indexNames.length == languages.length, s"Length mismatch: $indexNames, $languages")

  override def toString: String = s"GitRepository(url=$url, languages=$languages, docs=$docs)"

  /** Converts this GitRepository to a RepositoryWithCategories
    * @return A RepositoryWithCategories derived from this repository
    */
  def toRepositoryWithCategories(): RepositoryWithCategories =
    RepositoryWithCategories(url, languages.map(_.category).distinct, docs)

/** Utility object for GitRepository operations */
object GitRepository extends LazyLogging:
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

  /** Creates a list of repositories with categories from a list of QdrantURLs
    * @param qdrantCollections List of QdrantURLs to process
    * @return List of RepositoryWithCategories
    */
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

  /** Detects multiple languages from a comma-separated string
    * @param languages Comma-separated list of language identifiers
    * @return List of detected Language enum values
    */
  def detectLanguages(languages: String): List[Language] =
    languages.split(",")
      .toList
      .flatMap(l => findLanguage(l.trim))
      .distinctBy(_.toString)

  /** Detects a language from a file path by its extension
    * @param filePath Path to the file
    * @return Some(Language) if detected, None otherwise
    */
  def detectLanguageFromFile(filePath: String): Option[Language] =
    val language = filePath.split("\\.").last
    findLanguage(language)

/** Represents a code file with content and metadata */
final case class CodeFile(
  content: String,
  language: Language,
  path: String,
  chunkSize: Int,
  chunkOverlap: Int,
):
  /** Creates a Langchain Document from this CodeFile
    * @return Some(Document) if the content is non-empty, None otherwise
    */
  def createLangchainDocument(): Option[Document] =
    Option(content.trim)
      .filter(_.nonEmpty)
      .map(
        trimmedContent => {
          val metadata = Metadata.from(JMap.of(
            "file_name",
            path,
            "code",
            language.toString,
            "chunk_size",
            chunkSize,
            "chunk_overlap",
            chunkOverlap,
          ))
          Document.from(trimmedContent, metadata)
        },
      )
