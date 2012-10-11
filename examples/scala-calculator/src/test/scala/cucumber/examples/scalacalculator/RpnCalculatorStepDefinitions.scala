package cucumber.examples.scalacalculator

import cucumber.api.scala.{ScalaDsl, EN}
import junit.framework.Assert._

class RpnCalculatorStepDefinitions extends ScalaDsl with EN {

  val calc = new RpnCalculator

  When("""^I add (\d+) and (\d+)$"""){ (arg1: Double, arg2: Double) =>
    calc push arg1
    calc push arg2
    calc push "+"
  }

  Then("^the result is (\\d+)$") { expected: Double =>
    assertEquals(expected, calc.value)
  }

  Before("~@foo"){
    println("Runs before scenarios *not* tagged with @foo")
  }
}