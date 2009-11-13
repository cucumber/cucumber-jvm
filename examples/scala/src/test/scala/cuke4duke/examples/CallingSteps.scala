package cuke4duke.examples

import cuke4duke.ScalaDsl

class CallingSteps extends ScalaDsl {
  When("^I call another step$") { () =>
    Given("it is magic")
  }
}