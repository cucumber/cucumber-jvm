package cuke4duke.internal.scala

import cuke4duke.internal.language.AbstractHook

import org.jruby.runtime.builtin.IRubyObject

import _root_.java.lang.{Throwable, String}

class ScalaHook(tagNames:Array[String], f:() => Unit) extends AbstractHook(tagNames) {

  @throws(classOf[Throwable])
  def invoke(location: String, scenario: IRubyObject){
    //add support for f:(Scenario) => Unit // when there will be a Scenario class
    f()
  }
}