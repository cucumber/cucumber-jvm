package cuke4duke.internal.scala

import cuke4duke.internal.language.AbstractHook

import org.jruby.runtime.builtin.IRubyObject

import _root_.java.lang.{Throwable, String}

class ScalaHook(tagNames:Array[String], f:() => Unit) extends AbstractHook(tagNames) {

  @throws(classOf[Throwable])
  def invoke(location: String, scenario: IRubyObject){
    //legg til støtte for f:(Scenario) => Unit
    f()
  }
}