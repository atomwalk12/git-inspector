package gitinsp.tests.contentformatter

import gitinsp.domain.models.AIServiceURL
import gitinsp.domain.models.Category
import gitinsp.domain.models.QdrantURL
import gitinsp.domain.models.URL
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class URLServiceSpec extends AnyFlatSpec with Matchers:

  "URLService" should "convert a URL to a textual AIService URL" in:
    // Setup
    val url = URL("https://github.com/atom12/PPS-22-git-insp")

    // Test URL → AIServiceURL
    val aiServiceURL = url.toAIServiceURL()
    aiServiceURL should be(AIServiceURL("github.com[slash]atom12[slash]PPS-22-git-insp"))

    // Test AIServiceURL → QdrantURL
    val category  = Category.TEXT
    val qdrantURL = aiServiceURL.toQdrantURL(category)
    qdrantURL should be(QdrantURL("github.com[slash]atom12[slash]PPS-22-git-insp-text", category))

    // Test QdrantURL → AIServiceURL
    val aiServiceURL2 = qdrantURL.buildAIServiceURL()
    aiServiceURL2 should be(AIServiceURL("github.com[slash]atom12[slash]PPS-22-git-insp"))

    // Test AIServiceURL → URL
    val url2 = aiServiceURL2.toURL()

  it should "fail if the URL is not valid" in:
    // Execute and verify
    assertThrows[AssertionError] {
      AIServiceURL("github.com.atom12.PPS-22-git-insp") // missing slash
    }

    // Execute and verify
    assertThrows[AssertionError] {
      QdrantURL("github.com.atom12.PPS-22-git-insp") // doesn't end with -text or -code
    }

    // Execute and verify
    assertThrows[AssertionError] {
      URL("github/atom12.PPS-22-git-insp") // invalid URL
    }
