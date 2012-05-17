package cucumber
package runtime

import gherkin.TagExpression
import gherkin.formatter.model.Tag
import collection.JavaConverters._
import _root_.java.util.Collection

class ScalaHookDefinition(f:() => Unit, order:Int, tags:Seq[String]) extends HookDefinition {
  val tagExpression = new TagExpression(tags.asJava)

  def getLocation(detail: Boolean) = "TODO: Implement getLocation in similar fashion to ScalaStepDefinition"

  def execute(scenarioResult: ScenarioResult) { f() }

  def matches(tags: Collection[Tag]) = tagExpression.eval(tags)

  def getOrder = order
}