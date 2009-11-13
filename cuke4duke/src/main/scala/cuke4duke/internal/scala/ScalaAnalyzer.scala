package cuke4duke.internal.scala

import _root_.java.lang.reflect.Method
import cuke4duke.internal.jvmclass.{ClassLanguage, ObjectFactory, ClassAnalyzer}
import cuke4duke.{StepMother, ScalaDsl}

class ScalaAnalyzer extends ClassAnalyzer {

  def alwaysLoad = Array(classOf[ScalaTransformations])

  def populateStepDefinitionsAndHooksFor(method: Method, objectFactory:ObjectFactory, classLanguage:ClassLanguage) {
    if (classOf[ScalaDsl].isAssignableFrom(method.getDeclaringClass)) {

      val scalaDsl = objectFactory.getComponent(method.getDeclaringClass).asInstanceOf[ScalaDsl]
      val transformations = objectFactory.getComponent(classOf[ScalaTransformations]).asInstanceOf[ScalaTransformations]

      transformations.addAll(scalaDsl.transformations)

      for (before <- scalaDsl.beforeHooks)
        classLanguage.addBeforeHook(before)

      for (stepDefinition <- scalaDsl.stepDefinitions)
        classLanguage.addStepDefinition(stepDefinition(classLanguage, transformations))

      for (after <- scalaDsl.afterHooks)
        classLanguage.addAfterHook(after)

      scalaDsl.executionMode(objectFactory.getComponent(classOf[StepMother]).asInstanceOf[StepMother])
    }
  }
  
}