package cucumber.examples.scalacalculator

import org.junit.runner.RunWith
import cucumber.junit.Cucumber.Options
import cucumber.junit.{Options, Cucumber}

@RunWith(classOf[Cucumber])
@Options("basic_arithmetic.feature")
class RunCukesTest