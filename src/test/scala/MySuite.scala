import org.scalatest.flatspec.AnyFlatSpec

class FirstSpec extends AnyFlatSpec {
  "Hello" should "have length 5" in {
    val str = "Hello"
    assert(str.length == 5)
  }
}