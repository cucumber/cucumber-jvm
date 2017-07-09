package cucumber.runtime.scala

import gherkin.pickles.PickleTag
import java.util.Collection
import cucumber.api.Scenario
import cucumber.runtime.HookDefinition
import cucumber.runtime.TagPredicate
import scala.collection.JavaConverters._

class ScalaHookDefinition(f:Scenario => Unit,
                          order:Int,
                          tags:Seq[String]) extends HookDefinition {

  val tagPredicate = new TagPredicate(tags.asJava)

  def getLocation(detail: Boolean) = "TODO: Implement getLocation in similar fashion to ScalaStepDefinition"

  def execute(scenario: Scenario) { f(scenario) }

  def matches(tags: Collection[PickleTag]) = tagPredicate.apply(tags)

  def getOrder = order

  def isScenarioScoped = false
}