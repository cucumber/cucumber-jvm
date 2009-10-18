package cuke4duke.internal.scala

import _root_.java.lang.Class
import cuke4duke.ScalaDsl
import cuke4duke.internal.jvmclass.{ClassLanguage, ObjectFactory, ClassAnalyzer}

class ScalaAnalyzer extends ClassAnalyzer {

  def populateStepDefinitionsAndHooksFor(clazz: Class[_], objectFactory:ObjectFactory, classLanguage:ClassLanguage) {
    if (classOf[ScalaDsl].isAssignableFrom(clazz)) {
      val scalaDsl = objectFactory.getComponent(clazz).asInstanceOf[ScalaDsl]

      for (before <- scalaDsl.beforeHooks)
        classLanguage.addBeforeHook(before)

      for (stepDefinition <- scalaDsl.stepDefinitions)
        classLanguage.addStepDefinition(stepDefinition(classLanguage))

      for (after <- scalaDsl.afterHooks)
        classLanguage.addAfterHook(after)
    }
  }
  
  def addDefaultTransforms(objectFactory:ObjectFactory, classLanguage:ClassLanguage) {
      
  }
}