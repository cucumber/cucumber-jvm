package cucumber.api.scala

import _root_.cucumber.api.Scenario
import _root_.cucumber.runtime.scala.ScalaHookDefinition
import _root_.cucumber.runtime.scala.ScalaStepDefinition
import _root_.cucumber.runtime.HookDefinition
import _root_.cucumber.runtime.StepDefinition
import collection.mutable.ArrayBuffer

/**
 * Base trait for a scala step definition implementation.
 */
trait ScalaDsl { self =>

  /**
   *
   */
  private [cucumber] val stepDefinitions = new ArrayBuffer[StepDefinition]

  /**
   *
   */
  private [cucumber] val beforeHooks = new ArrayBuffer[HookDefinition]

  /**
   *
   */
  private [cucumber] val afterHooks = new ArrayBuffer[HookDefinition]

  def Before(f: Scenario => Unit){
    Before()(f)
  }

  def Before(tags: String*)(f: Scenario => Unit) {
    Before(Int.MaxValue, tags :_*)(f)
  }

  def Before(order:Int, tags:String*)(f: Scenario => Unit){
    beforeHooks += new ScalaHookDefinition(f, order, tags)
  }

  def After(f: Scenario => Unit){
    After()(f)
  }

  def After(tags: String*)(f: Scenario => Unit) {
    After(Int.MaxValue, tags:_*)(f)
  }

  def After(order:Int, tags: String*)(f: Scenario => Unit){
    afterHooks += new ScalaHookDefinition(f, order, tags)
  }

  final class Step(name: String) {
    def apply(regex: String): StepBody = new StepBody(name, regex)
  }

  final class Fun0(val f: Function0[Any])

  object Fun0 {
    implicit def function02Fun0(f: Function0[Any]) = new Fun0(f)
  }

  final class StepBody(name:String, regex:String) {

    def apply(f: => Unit){ apply(f _) }

    def apply(fun: Fun0) = register(Nil) {case Nil => fun.f()}

    def apply[T1](f: (T1) => Any)(implicit m1: Manifest[T1]) = {
      register(List(m1)) {
        case List(a1:Any) => f(a1:T1)
        //case List(a1:Any) => throw new MatchError("Expected " + m1.toString() + " but got " + a1.getClass)
      }
    }

    def apply[T1, T2](f: (T1, T2) => Any)(implicit m1: Manifest[T1], m2: Manifest[T2]) = {
      register(List(m1, m2)) { case List(a1:T1, a2:T2) => f(a1, a2) }
    }

    private def register(manifests: List[Manifest[_]])(pf: PartialFunction[List[Any], Any]){
      val frame: StackTraceElement = obtainFrame
      stepDefinitions += new ScalaStepDefinition(frame, name, regex, manifests.map(_.runtimeClass), pf)
    }

    private def obtainFrame: StackTraceElement = {
      val frames = Thread.currentThread().getStackTrace
      val currentClass = self.getClass.getName
      frames.find(_.getClassName == currentClass).get
    }
  }
}
