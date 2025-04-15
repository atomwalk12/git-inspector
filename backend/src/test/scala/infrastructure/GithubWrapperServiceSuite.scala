package gitinsp.tests.domain

import com.typesafe.config.Config
import gitinsp.domain.interfaces.infrastructure.FetchingService
import gitinsp.domain.models.CodeFile
import gitinsp.domain.models.Language
import gitinsp.domain.models.RepositoryWithLanguages
import gitinsp.domain.models.URL
import gitinsp.infrastructure.GithubWrapperService
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.spy
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar

import scala.util.Failure
import scala.util.Success

class GithubWrapperServiceSuite
    extends AnyFlatSpec
    with Matchers
    with BeforeAndAfterAll
    with MockitoSugar:

  it should "handle invalid repository URLs gracefully" in:
    // Create a mock Config
    val mockConfig = mock[Config]
    val urlClient  = mock[FetchingService]
    val languages  = RepositoryWithLanguages.detectLanguage("scala").getOrElse(List())

    // Setup behavior
    val exception = new Exception("Could not fetch URL")
    when(mockConfig.getInt("gitinsp.timeout")).thenReturn(5000)
    when(urlClient.fetchUrl(any, any, any, any, any)).thenReturn(Failure(exception))

    // Setup data
    val service    = GithubWrapperService(mockConfig, urlClient)
    val invalidUrl = "https://invalid-url-that-doesnt-exist.com"

    // Execute
    val result = service.buildRepository(URL(invalidUrl), languages)

    // Verify
    result.isFailure shouldBe true

  // Testing with a mock HttpURLConnection
  it should "create a GitRepository from GitHub JSON response" in:
    // Create a mock Config
    val mockConfig  = mock[Config]
    val urlClient   = mock[FetchingService]
    val testService = spy(GithubWrapperService(mockConfig, urlClient))

    // Setup test data
    val testUrl   = URL("https://test-github-url.com/atomwalk12")
    val testJson  = """
      {
        "files": {
          "test.scala": {
            "content": "object Test { def main(args: Array[String]): Unit = println(\"Hello\") }"
          }
        }
      }
    """
    val languages = RepositoryWithLanguages.detectLanguage("scala").getOrElse(List())
    val docs      = List(CodeFile("test content", Language.SCALA, "test.scala", 1000, 100))
    val repo      = RepositoryWithLanguages(testUrl, languages, docs)

    // Behavior
    when(mockConfig.getInt("gitinsp.timeout")).thenReturn(5000)
    when(urlClient.fetchUrl(any, any, any, any, any)).thenReturn(Success(testJson))

    // Execute
    val result = testService.buildRepository(testUrl, languages)

    // Verify
    result.isSuccess shouldBe true
    result match
      case Success(repo) =>
        repo.url shouldBe testUrl
        repo.languages should contain(Language.SCALA)
        repo.docs.headOption match
          case Some(doc) => doc.path shouldBe "test.scala"
          case None      => fail("Expected at least one document in the repository")
      case Failure(ex) =>
        fail(s"Repository fetch failed: ${ex.getMessage}")

  it should "create a GitRepository from GitHub plain text response" in:
    // Create a mock Config
    val mockConfig  = mock[Config]
    val urlClient   = mock[FetchingService]
    val testService = spy(GithubWrapperService(mockConfig, urlClient))

    // Setup test data
    val testUrl   = URL("https://test-github-url.com")
    val testJson  = """
      {
        "files": {
          "test.scala": {
            "content": "object Test { def main(args: Array[String]): Unit = println(\"Hello\") }"
          }
        }
      }
    """
    val languages = RepositoryWithLanguages.detectLanguage("scala").getOrElse(List())

    // Behavior
    when(mockConfig.getInt("gitinsp.timeout")).thenReturn(5000)
    when(urlClient.fetchUrl(any, any, any, any, any)).thenReturn(Success(testJson))

    // Execute
    val result = testService.fetchRepository(testUrl, languages)

    // Verify
    result.isSuccess shouldBe true
    result match
      case Success(plainText) =>
        plainText shouldBe testJson
      case Failure(ex) =>
        fail(s"Repository fetch failed: ${ex.getMessage}")
