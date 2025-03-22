// For more information on writing tests, see
import org.scalatest.flatspec.AnyFlatSpec

// https://scalameta.org/munit/docs/getting-started.html
class FirstSpec extends AnyFlatSpec {
  "The GitInspector" should "correctly analyze a git repository" in {
    val str = "Hello"
    assert(str.length == 5)
  }
}