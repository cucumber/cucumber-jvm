package cucumber.runtime

import junit.framework.Assert._

class RpnCalculatorStepDefinitions extends ScalaDsl with EN {
    Given("""^I have (\d+) "([^"]*)" in my belly$"""){ (arg0:Int, arg1:String) =>
    }

    Then("""^I am "([^"]*)"$"""){ (arg0:String) =>
    }
}
