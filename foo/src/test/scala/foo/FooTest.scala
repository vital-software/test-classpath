package foo

import org.scalatest._

class FooTest extends FlatSpec with Matchers {
  "Test resources" should "not contain resources from the subproject 'bar'" in {
    getClass.getResource("/bar.conf") should be(null)
  }
}
