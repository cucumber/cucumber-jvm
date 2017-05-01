package cucumber.runtime.scala

import gherkin.TagExpression
import gherkin.formatter.model.Tag
import java.util.Collection
import cucumber.api.Scenario
import cucumber.runtime.HookDefinition
import collection.JavaConverters._

class ScalaHookDefinition(f:Scenario => Unit,
                          order:Int,
                          tags:Seq[String]) extends HookDefinition {

  val tagExpression = new TagExpression(tags.asJava)

  def getLocation(detail: Boolean) = "TODO: Implement getLocation in similar fashion to ScalaStepDefinition"

  def execute(scenario: Scenario) { f(scenario) }

  def matches(tags: Collection[Tag]) = tagExpression.evaluate(tags)

  def getOrder = order

  def isScenarioScoped = false
}