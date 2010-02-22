package cuke4duke.examples

import org.junit.Assert.assertTrue
import cuke4duke.scala.{Dsl, EN}

class CalledSteps extends Dsl with EN {
  var magic = false

  Given("^it is (.*)$") { what: String =>
    if ("magic" == what)
      magic = true
  }

  Then("^magic should happen$") {
    assertTrue(magic);
  }
}