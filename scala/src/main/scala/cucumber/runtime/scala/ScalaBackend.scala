package cucumber.runtime.scala

import _root_.java.util.{List => JList}
import _root_.gherkin.formatter.model.Step
import _root_.java.lang.reflect.Modifier
import _root_.cucumber.runtime.snippets.SnippetGenerator
import _root_.cucumber.runtime.snippets.FunctionNameSanitizer
import _root_.cucumber.api.scala.ScalaDsl
import _root_.cucumber.runtime.ClassFinder
import _root_.cucumber.runtime.io.ResourceLoaderClassFinder
import _root_.cucumber.runtime.io.ResourceLoader
import _root_.cucumber.runtime.io.MultiLoader
import _root_.cucumber.runtime.Backend
import _root_.cucumber.runtime.UnreportedStepExecutor
import _root_.cucumber.runtime.Glue
import collection.JavaConversions._
import _root_.scala.collection.Map
import _root_.cucumber.runtime.CucumberException;
import _root_.cucumber.runtime.Reflections;
import _root_.cucumber.runtime.io.ResourceLoaderClassFinder;
import _root_.cucumber.hiddenruntime.scala.DefaultScalaObjectFactory

class ScalaBackend(resourceLoader: ResourceLoader) extends Backend {
  private var snippetGenerator = new SnippetGenerator(new ScalaSnippetGenerator())
  private var instances: Seq[ScalaDsl] = Nil
  private var objectFactory: ObjectFactory = loadObjectFactory()

  private val classFinder: ClassFinder = new ResourceLoaderClassFinder(resourceLoader, Thread.currentThread().getContextClassLoader())
  private val reflections: Reflections = new Reflections(classFinder)

  def getStepDefinitions = instances.flatMap(_.stepDefinitions)

  def getBeforeHooks = instances.flatMap(_.beforeHooks)

  def getAfterHooks = instances.flatMap(_.afterHooks)

  def disposeWorld() {
	  instances = Nil
			  objectFactory.stop();
  }

  def loadObjectFactory(): ObjectFactory = {
    try {
      reflections.instantiateExactlyOneSubclass(classOf[ObjectFactory], "cucumber.runtime", Array[Class[_]](), Array[Object]())
    } catch {
      case x: CucumberException => new DefaultScalaObjectFactory();
    }
  }

  def getSnippet(step: Step, functionNameSanitizer: FunctionNameSanitizer) = snippetGenerator.getSnippet(step, functionNameSanitizer)

  def buildWorld() {
    objectFactory.start();
  }

  def loadGlue(glue: Glue, gluePaths: JList[String]) {
    val cl = Thread.currentThread().getContextClassLoader
    val classFinder = new ResourceLoaderClassFinder(resourceLoader, cl)
    val packages = gluePaths map { cucumber.runtime.io.MultiLoader.packageName(_) }
    val dslClasses = packages flatMap { classFinder.getDescendants(classOf[ScalaDsl], _) } filter { cls =>
      // all descendants of ScalaDSL are selected 
      true
    }
    val (clsClasses, objClasses) = dslClasses partition { cls =>
      try {
//        has Module? - is Object
        cls.getDeclaredField("MODULE$")
        false
      } catch {
        case e : Throwable => true
      }
    }
    val objInstances = objClasses map {cls =>
      val instField = cls.getDeclaredField("MODULE$")
      instField.setAccessible(true)
      instField.get(null).asInstanceOf[ScalaDsl]
    }
    val clsInstances = (clsClasses map {objectFactory.getInstance(_)}) 

    instances = objInstances ++ clsInstances

    getStepDefinitions map {glue.addStepDefinition(_)}
    getBeforeHooks map {glue.addBeforeHook(_)}
    getAfterHooks map {glue.addAfterHook(_)}
  }

  def setUnreportedStepExecutor(executor:UnreportedStepExecutor) {}
}
