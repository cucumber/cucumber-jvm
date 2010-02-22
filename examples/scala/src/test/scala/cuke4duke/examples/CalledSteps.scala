package cuke4duke.examples

import org.junit.Assert.assertTrue
import cuke4duke.{English, ScalaDsl}

class CalledSteps extends ScalaDsl with English {
  var magic = false

  Given("^it is (.*)$") { what: String =>
    if ("magic" == what)
      magic = true
  }

  Then("^magic should happen$") {
    assertTrue(magic);
  }
}