package cucumber
package runtime

import gherkin.formatter.model.Step
import resources.Resources

import collection.JavaConverters._

class ScalaBackend(packagePrefixes:java.util.List[String]) extends Backend {

  // TODO: loop over all packagePrefixes. Don't know enough Scala syntax (AH)
  val instances = Resources.instantiateSubclasses(classOf[ScalaDsl], packagePrefixes.get(0), Array(), Array()).asScala

  def getStepDefinitions = instances.flatMap(_.stepDefinitions).asJava

  def getBeforeHooks = instances.flatMap(_.beforeHooks).asJava

  def getAfterHooks = instances.flatMap(_.afterHooks).asJava

  def newWorld() {}

  def disposeWorld() {}

  def getSnippet(step: Step) = new ScalaSnippetGenerator(step).getSnippet
}