package cucumber.runtime.scala.test

import _root_.cucumber.api.scala._

import cucumber.api.DataTable

object RpnCalculatorStepDefinitions extends ScalaDsl with EN {
    Given("""^I have (\d+) "([^"]*)" in my belly$"""){ (howMany:Int, what:String) =>
    }

    Given("""^I have the following foods :$"""){ (table:DataTable) =>
    }
}

class ThenDefs extends ScalaDsl with EN {
  Then("""^I am "([^"]*)"$"""){ (arg0:String) =>
  }
}