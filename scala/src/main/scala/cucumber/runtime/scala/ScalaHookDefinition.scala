package cucumber.runtime.scala

import _root_.cucumber.api.Scenario
import _root_.cucumber.runtime.HookDefinition

class ScalaHookDefinition(f:Scenario => Unit,
                          order:Int,
                          tagExpression:String) extends HookDefinition {

  def getLocation(detail: Boolean) = "TODO: Implement getLocation in similar fashion to ScalaStepDefinition"

  def execute(scenario: Scenario) { f(scenario) }

  def getTagExpression = tagExpression

  def getOrder = order
}
