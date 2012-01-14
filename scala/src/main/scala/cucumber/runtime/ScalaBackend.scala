package cucumber
package runtime

import _root_.java.util.{List => JList}

import gherkin.formatter.model.Step
import snippets.SnippetGenerator
import io.ResourceLoader
import io.ClasspathResourceLoader

import scala.collection.JavaConversions._

class ScalaBackend(ignore:ResourceLoader) extends Backend {
  private var snippetGenerator = new SnippetGenerator(new ScalaSnippetGenerator())
  private var instances:Seq[ScalaDsl] = Nil 

  def getStepDefinitions = instances.flatMap(_.stepDefinitions)

  def getBeforeHooks = instances.flatMap(_.beforeHooks)

  def getAfterHooks = instances.flatMap(_.afterHooks)

  def disposeWorld() {
    instances = Nil
  }

  def getSnippet(step: Step) = snippetGenerator.getSnippet(step)

  def buildWorld(gluePaths: JList[String], world: World) {
    instances = gluePaths flatMap { new ClasspathResourceLoader().instantiateSubclasses(classOf[ScalaDsl], _, Array(), Array()) }

    getStepDefinitions map {world.addStepDefinition(_)}
    getBeforeHooks map {world.addBeforeHook(_)}
    getAfterHooks map  {world.addAfterHook(_)}
  }
}
