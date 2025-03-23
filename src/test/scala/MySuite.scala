import org.scalatest.flatspec.AnyFlatSpec

import org.scalatest.Tag
object FastTest   extends Tag("Fast")
object MediumTest extends Tag("Medium")

class FirstSpec extends AnyFlatSpec:

  "Hello" should "have length 5" taggedAs FastTest in:
    val str = "Hello"
    assert(str.length == 5)
