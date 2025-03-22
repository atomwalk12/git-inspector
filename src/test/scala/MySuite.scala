// For more information on writing tests, see
// https://scalameta.org/munit/docs/getting-started.html
import org.scalatest.flatspec.AnyFlatSpec

class FirstSpec extends AnyFlatSpec {
  "Hello" should "have length 5" in {
    val str = "Hello"
    assert(str.length == 5)
  }
}