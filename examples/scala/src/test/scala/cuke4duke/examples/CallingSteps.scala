package cuke4duke.examples

import cuke4duke.{English, ScalaDsl}

class CallingSteps extends ScalaDsl with English {
  When("^I call another step$") { () =>
    Given("it is magic")
  }
}