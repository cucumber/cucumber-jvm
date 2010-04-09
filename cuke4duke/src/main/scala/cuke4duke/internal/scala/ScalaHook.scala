package cuke4duke.internal.scala

import cuke4duke.internal.language.AbstractHook
import cuke4duke.Scenario

import _root_.java.lang.{Throwable, String}

class ScalaHook(tagNames:Array[String], f:() => Unit) extends AbstractHook(tagNames) {

  @throws(classOf[Throwable])
  def invoke(location: String, scenario: Scenario){
    //add support for f:(Scenario) => Unit // when there will be a Scenario class
    f()
  }
}