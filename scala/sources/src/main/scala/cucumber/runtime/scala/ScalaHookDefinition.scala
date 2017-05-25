package cucumber.runtime.scala

import _root_.gherkin.pickles.PickleTag
import _root_.java.util.Collection
import _root_.cucumber.api.Scenario
import _root_.cucumber.runtime.HookDefinition
import _root_.cucumber.runtime.TagPredicate
import collection.JavaConverters._

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