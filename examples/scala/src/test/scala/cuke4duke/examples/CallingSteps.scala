package cuke4duke.examples

import cuke4duke._

class CallingSteps extends ScalaDsl with EN {
  When("^I call another step$") { () =>
    Given("it is magic")
  }
}