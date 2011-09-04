package cucumber
package runtime

import gherkin.TagExpression
import collection.JavaConverters._
import _root_.java.util.Collection

class ScalaHookDefinition(f:() => Unit, order:Int, tags:Seq[String]) extends HookDefinition {
  val tagExpression = new TagExpression(tags.asJava)

  def execute(scenarioResult: ScenarioResult) { f() }

  def matches(tags: Collection[String]) = tagExpression.eval(tags)

  def getOrder = order
}