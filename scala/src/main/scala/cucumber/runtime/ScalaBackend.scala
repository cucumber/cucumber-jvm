package cucumber
package runtime

import _root_.java.util.{List => JList}

import gherkin.formatter.model.Step
import resources.Resources

import collection.JavaConverters._

class ScalaBackend(packagePrefixes:JList[String]) extends Backend {

  private var instances:Seq[ScalaDsl] = Nil 

  def getStepDefinitions = instances.flatMap(_.stepDefinitions).asJava

  def getBeforeHooks = instances.flatMap(_.beforeHooks).asJava

  def getAfterHooks = instances.flatMap(_.afterHooks).asJava

  def newWorld() {
    instances = packagePrefixes.asScala.flatMap { Resources.instantiateSubclasses(classOf[ScalaDsl], _, Array(), Array()).asScala }  
  }

  def disposeWorld() {
    instances = Nil
  }

  def getSnippet(step: Step) = new ScalaSnippetGenerator(step).getSnippet
}
