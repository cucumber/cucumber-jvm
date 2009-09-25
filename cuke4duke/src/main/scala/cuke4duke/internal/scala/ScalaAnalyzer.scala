package cuke4duke.internal.scala

import _root_.java.lang.Class
import _root_.java.util.{List => JList}
import cuke4duke.ScalaDsl
import cuke4duke.internal.language.{StepDefinition, Hook}
import cuke4duke.internal.jvmclass.{ObjectFactory, ClassAnalyzer}

class ScalaAnalyzer extends ClassAnalyzer {

  def populateStepDefinitionsAndHooksFor(clazz: Class[_], objectFactory:ObjectFactory, befores: JList[Hook], stepDefinitions: JList[StepDefinition], afters: JList[Hook]) {
    if (classOf[ScalaDsl].isAssignableFrom(clazz)) {
      val scalaDsl = objectFactory.getComponent(clazz).asInstanceOf[ScalaDsl]

      for (stepDefinition <- scalaDsl.stepDefinitions)
        stepDefinitions.add(stepDefinition)

      for (before <- scalaDsl.beforeHooks)
        befores.add(before)

      for (after <- scalaDsl.afterHooks)
        afters.add(after)
    }
  }
}