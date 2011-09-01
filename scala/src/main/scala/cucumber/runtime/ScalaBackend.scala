package cucumber
package runtime

import gherkin.formatter.model.Step
import resources.Resources

import collection.JavaConverters._

class ScalaBackend(packagePrefix:String) extends Backend {

  val instances = Resources.instantiateSubclasses(classOf[ScalaDsl], packagePrefix).asScala

  def getStepDefinitions = instances.flatMap(_.stepDefinitions).asJava

  def getBeforeHooks = instances.flatMap(_.beforeHooks).asJava

  def getAfterHooks = instances.flatMap(_.afterHooks).asJava

  def newWorld() {}

  def disposeWorld() {}

  def getSnippet(step: Step) = new ScalaSnippetGenerator(step).getSnippet
}