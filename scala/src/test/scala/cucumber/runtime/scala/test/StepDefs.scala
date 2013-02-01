package cucumber.runtime.scala.test

import _root_.cucumber.api.scala._

import cucumber.api.DataTable
import junit.framework.Assert._
import scala.collection.JavaConversions._

object CukesStepDefinitions extends ScalaDsl with EN {

  var calorieCount = 0.0

  Given("""^I have (\d+) "([^"]*)" in my belly$"""){ (howMany:Int, what:String) =>
  }

  Given("""^I have the following foods :$"""){ (table:DataTable) =>
    calorieCount = table.asMaps().map(_.values()).map(_.head.toDouble).fold(0.0)(_+_)
  }

  And("""^have eaten (.*) calories today""") { (calories:Double) =>
    assertEquals(calorieCount, calories)
  }
}

class ThenDefs extends ScalaDsl with EN {
  Then("""^I am "([^"]*)"$"""){ (arg0:String) =>
  }
}