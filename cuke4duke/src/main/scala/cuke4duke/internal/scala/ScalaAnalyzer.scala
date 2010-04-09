package cuke4duke.internal.scala

import cuke4duke.{StepMother}
import cuke4duke.ScalaDsl
import cuke4duke.internal.language.AbstractProgrammingLanguage
import cuke4duke.internal.jvmclass.{ClassLanguage, ClassAnalyzer, ObjectFactory}

/**
 * Collects hooks and step definitions from all classes with the 'ScalaDsl' trait.
 * After collection it sets the 'ScalaDsl' instance in execution-mode
 */
class ScalaAnalyzer extends ClassAnalyzer {
  def alwaysLoad = Array()

  def populateStepDefinitionsAndHooks(objectFactory:ObjectFactory, classLanguage:ClassLanguage) {
    //ugly, but works on both 2.7.x and 2.8
    var dsls:List[ScalaDsl] = Nil
    val iterator = classLanguage.getClasses.iterator
    while(iterator.hasNext){
      val next = iterator.next
      if(classOf[ScalaDsl].isAssignableFrom(next))
        dsls ::= objectFactory.getComponent(next.asInstanceOf[Class[ScalaDsl]])
    }
    populate(dsls, objectFactory.getComponent(classOf[StepMother]), classLanguage)
  }

  def populate(dsls: List[ScalaDsl], stepMother:StepMother, language:AbstractProgrammingLanguage){
    for(dsl <- dsls){
      for (before <- dsl.beforeHooks)
        language.addBeforeHook(before)

      for (stepDefinition <- dsl.stepDefinitions)
        language.addStepDefinition(stepDefinition(language))

      for (after <- dsl.afterHooks)
        language.addAfterHook(after)

      // putting dsl into execution mode - to support calling steps from steps
      dsl.executionMode(stepMother)
    }
  }
}