package cucumber.runtime.scala.test

import _root_.cucumber.api.scala._

import _root_.junit.framework.Assert._

object RpnCalculatorStepDefinitions extends ScalaDsl with EN {
    Given("""^I have (\d+) "([^"]*)" in my belly$"""){ (howMany:Int, what:String) =>
    }
}

class ThenDefs extends ScalaDsl with EN {
  Then("""^I am "([^"]*)"$"""){ (arg0:String) =>
  }
}