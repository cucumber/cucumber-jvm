package cucumber.runtime.scala

import _root_.java.util.{List => JList}
import _root_.gherkin.pickles.PickleStep
import _root_.java.lang.reflect.Modifier
import _root_.cucumber.runtime.snippets.SnippetGenerator
import _root_.cucumber.runtime.snippets.FunctionNameGenerator
import _root_.cucumber.api.scala.ScalaDsl
import _root_.cucumber.runtime.ClassFinder
import _root_.cucumber.runtime.io.ResourceLoaderClassFinder
import _root_.cucumber.runtime.io.ResourceLoader
import _root_.cucumber.runtime.io.MultiLoader
import _root_.cucumber.runtime.Backend
import _root_.cucumber.runtime.UnreportedStepExecutor
import _root_.cucumber.runtime.Glue
import collection.JavaConversions._

class ScalaBackend(resourceLoader:ResourceLoader) extends Backend {
  private var snippetGenerator = new SnippetGenerator(new ScalaSnippetGenerator())
  private var instances:Seq[ScalaDsl] = Nil

  def getStepDefinitions = instances.flatMap(_.stepDefinitions)

  def getBeforeHooks = instances.flatMap(_.beforeHooks)

  def getAfterHooks = instances.flatMap(_.afterHooks)

  def disposeWorld() {
    instances = Nil
  }

  def getSnippet(step: PickleStep, keyword: String, functionNameGenerator: FunctionNameGenerator) = snippetGenerator.getSnippet(step, keyword, functionNameGenerator)

  def buildWorld() {
    //I don't believe scala has to do anything to clean out its world
  }

  def loadGlue(glue: Glue, gluePaths: JList[String]) {
    val cl = Thread.currentThread().getContextClassLoader
    val classFinder = new ResourceLoaderClassFinder(resourceLoader, cl)
    val packages = gluePaths map { cucumber.runtime.io.MultiLoader.packageName(_) }
    val dslClasses = packages flatMap { classFinder.getDescendants(classOf[ScalaDsl], _) } filter { cls =>
      try {
        cls.getDeclaredConstructor()
        true
      } catch {
        case e : Throwable => false
      }
    }
    val (clsClasses, objClasses) = dslClasses partition { cls =>
      try {
        Modifier.isPublic (cls.getConstructor().getModifiers)
      } catch {
        case e : Throwable  => false
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
