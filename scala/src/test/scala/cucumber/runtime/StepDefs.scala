package cucumber.runtime

import junit.framework.Assert._

object RpnCalculatorStepDefinitions extends ScalaDsl with EN {
    Given("""^I have (\d+) "([^"]*)" in my belly$"""){ (arg0:Int, arg1:String) =>
    }
}

class ThenDefs extends ScalaDsl with EN {
  Then("""^I am "([^"]*)"$"""){ (arg0:String) =>
  }
}