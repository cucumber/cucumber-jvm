package cuke4duke.examples

import cuke4duke.ScalaDsl
import org.junit.Assert.assertTrue

class CalledSteps extends ScalaDsl {
  var magic = false

  Given("^it is (.*)$") { what: String =>
    if ("magic" == what)
      magic = true
  }

  Then("^magic should happen$") {
    assertTrue(magic);
  }
}