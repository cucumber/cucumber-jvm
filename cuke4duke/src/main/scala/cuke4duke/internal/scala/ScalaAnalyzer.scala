package cuke4duke.internal.scala

import _root_.java.lang.Class
import cuke4duke.internal.jvmclass.{ClassLanguage, ClassAnalyzer}
import cuke4duke.ScalaDsl

class ScalaAnalyzer extends ClassAnalyzer {
  def registerHooksAndStepDefinitionsFor(clazz: Class[_], classLanguage: ClassLanguage) {
    if (classOf[ScalaDsl].isAssignableFrom(clazz)) {
      /*
      We get here before classes are registered in the objectFactory (PicoContainer/Spring).
      Therefore, we can't ask the classLanguage for an instance of clazz yet.
      (We'll get a NullPointerException from PicoFactory.getComponent())

      Even if we changed ClassLanguage.load to objectFactory.addClass(clazz)
      before invoking this method, we would run into problems if a class
      with step definitions is abstract (they cannot be instantiated!)

      Some people (using Java at least) actually define step definitions in
      abstract classes. See Github ticket:
      http://github.com/aslakhellesoy/cuke4duke/issues/closed#issue/15

      Therefore - it should be possible to inspect step definitions of a class
      *without* an instance - simply by looking at the clazz.
      Is that possible?

      Short answer: No, like the Groovy version, an instance needs to be created since
      the stepdefinitions are closures.
       */
      val scalaDsl = classLanguage.getTarget(clazz).asInstanceOf[ScalaDsl]

      for (stepDefinition <- scalaDsl.stepDefinitions)
        classLanguage.addStepDefinition(stepDefinition, this)

      for (before <- scalaDsl.beforeHooks)
        classLanguage.addHook("before", before, this)

      for (after <- scalaDsl.afterHooks)
        classLanguage.addHook("after", after, this)
    }
  }
}