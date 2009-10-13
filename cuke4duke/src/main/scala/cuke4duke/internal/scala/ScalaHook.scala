package cuke4duke.internal.scala

import cuke4duke.internal.language.AbstractHook

import org.jruby.runtime.builtin.IRubyObject

import _root_.java.lang.{Throwable, String}
import _root_.java.util.{ArrayList}

class ScalaHook(tagNames:List[String], f:() => Unit) extends AbstractHook(new ArrayList[String]{ for(t <- tagNames) add(t) }) {

  @throws(classOf[Throwable])
  def invoke(location: String, scenario: IRubyObject){
    //legg til støtte for f:(Scenario) => Unit
    f()
  }
}