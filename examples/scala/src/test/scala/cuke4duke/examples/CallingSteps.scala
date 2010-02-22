package cuke4duke.examples

import cuke4duke.scala.{Dsl, EN}

class CallingSteps extends Dsl with EN {
  When("^I call another step$") { () =>
    Given("it is magic")
  }
}