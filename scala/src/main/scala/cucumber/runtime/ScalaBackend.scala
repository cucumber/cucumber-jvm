package cucumber
package runtime

import _root_.java.util.{List => JList}

import gherkin.formatter.model.Step
import _root_.java.lang.reflect.Modifier
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

  def buildWorld() {
    //I don't believe scala has to do anything to clean out it's world
  }

  def loadGlue(glue: Glue,  gluePaths: JList[String]) {
    val cl = new ClasspathResourceLoader(Thread.currentThread().getContextClassLoader)
    val dslClasses =  gluePaths flatMap {cl.getDescendants(classOf[ScalaDsl], _) } filter { cls =>
      try {
        cls.getDeclaredConstructor()
        true
      } catch {
        case e => false
      }
    }
    val (clsClasses, objClasses) = dslClasses partition { cls =>
      try {
        Modifier.isPublic (cls.getConstructor().getModifiers)
      } catch {
        case e => false
      }
    }
    val objInstances = objClasses map {cls =>
      val instField = cls.getDeclaredField("MODULE$")
      instField.setAccessible(true)
      instField.get(null).asInstanceOf[ScalaDsl]
    }
    val clsInstances = (clsClasses map {_.newInstance()})

    instances = objInstances ++ clsInstances

    getStepDefinitions map {glue.addStepDefinition(_)}
    getBeforeHooks map {glue.addBeforeHook(_)}
    getAfterHooks map  {glue.addAfterHook(_)}
  }

  def setUnreportedStepExecutor(executor:UnreportedStepExecutor) {}
}
