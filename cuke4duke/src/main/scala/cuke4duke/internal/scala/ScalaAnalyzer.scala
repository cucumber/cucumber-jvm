package cuke4duke.internal.scala

import cuke4duke.{StepMother}
import cuke4duke.internal.jvmclass.{ClassLanguage, ClassAnalyzer, ObjectFactory}
import cuke4duke.scala.Dsl

class ScalaAnalyzer extends ClassAnalyzer {
  def alwaysLoad = Array(classOf[ScalaTransformations])

  def populateStepDefinitionsAndHooks(objectFactory:ObjectFactory, classLanguage:ClassLanguage) {
    val it = classLanguage.getClasses.iterator;
    while(it.hasNext) {
      val clazz = it.next
      if (classOf[Dsl].isAssignableFrom(clazz)) {
        val scalaDsl = objectFactory.getComponent(clazz).asInstanceOf[Dsl]
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
}