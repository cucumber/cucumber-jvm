package cucumber.api.scala

import _root_.cucumber.api.Scenario
import _root_.cucumber.runtime.scala.ScalaHookDefinition
import _root_.cucumber.runtime.scala.ScalaStepDefinition
import _root_.cucumber.runtime.HookDefinition
import _root_.cucumber.runtime.StepDefinition
import collection.mutable.ArrayBuffer
import java.lang.reflect.Type

/**
 * Base trait for a scala step definition implementation.
 */
trait ScalaDsl { self =>
  import scala.language.implicitConversions

  private [cucumber] val stepDefinitions = new ArrayBuffer[StepDefinition]

  private [cucumber] val beforeHooks = new ArrayBuffer[HookDefinition]

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

    def apply(fun: Fun0) {
      register(Array[Type]()) {case Nil => fun.f()}
    }

    /**
     * Concerning the type handling in all apply methods below.
     *
     * Unfortunately the obvious approach (as follows) doesn't work as expected in all scenarios :
     *
     * {{{
     *   def apply[T1](f: (T1) => Any)(implicit m1:Manifest[T1]) = {
     *     register(List(m1)) {
     *       case List(a1:T1) => f(a1)
     *     }
     *   }
     * }}}
     *
     * This is due to the Scala 'Value Classes' being boxed when moving into the Java world,
     * and then returned into the Scala layer as these boxed types, which causes the following use case.
     *
     * - Step definition is defined with expected argument of scala type 'Int'
     * - Step is 'registered' passing the Scala Int type into the Java layer
     * - Inside the Java layer this is boxed from a 'primitive' into a java.lang.Integer
     * - The parsed value is returned into Scala layer, retaining it's boxed type
     * - When we perform the pattern match in the partial function, a List[Int] is expected but List[java.lang.Integer]
     *   is passed in and fails the match (causing a match exception)
     *
     * Therefore by casting we unbox the java.util.Integer (or other boxed type) back into the expected value working
     * around this issue - anything else should be the expected object anyway, so it makes no difference in that case.
     *
     * There's likely a cleaner way of doing this - please refactor if so.
     *
     */

    def apply[T1](f: (T1) => Any)(implicit m1:Manifest[T1]) {
      register(functionParams(f)) {
        case List(a1:AnyRef) =>
          f(a1.asInstanceOf[T1])
      }
    }

    def apply[T1, T2](f: (T1, T2) => Any)(implicit m1:Manifest[T1], m2:Manifest[T2]) {
      register(functionParams(f)) {
        case List(a1:AnyRef, a2:AnyRef) =>
          f(a1.asInstanceOf[T1],
            a2.asInstanceOf[T2])
      }
    }

    def apply[T1, T2, T3](f: (T1, T2, T3) => Any)(implicit m1:Manifest[T1], m2:Manifest[T2], m3:Manifest[T3]) {
      register(functionParams(f)) {
        case List(a1:AnyRef, a2:AnyRef, a3:AnyRef) =>
          f(a1.asInstanceOf[T1],
            a2.asInstanceOf[T2],
            a3.asInstanceOf[T3])
      }
    }

    def apply[T1, T2, T3, T4](f: (T1, T2, T3, T4) => Any)(implicit m1:Manifest[T1], m2:Manifest[T2], m3:Manifest[T3], m4:Manifest[T4]) {
      register(functionParams(f)) {
        case List(a1:AnyRef, a2:AnyRef, a3:AnyRef, a4:AnyRef) =>
          f(a1.asInstanceOf[T1],
            a2.asInstanceOf[T2],
            a3.asInstanceOf[T3],
            a4.asInstanceOf[T4])
      }
    }

    def apply[T1, T2, T3, T4, T5](f: (T1, T2, T3, T4, T5) => Any)(implicit m1:Manifest[T1], m2:Manifest[T2], m3:Manifest[T3], m4:Manifest[T4], m5:Manifest[T5]) {
      register(functionParams(f)) {
        case List(a1:AnyRef, a2:AnyRef, a3:AnyRef, a4:AnyRef, a5:AnyRef) =>
          f(a1.asInstanceOf[T1],
            a2.asInstanceOf[T2],
            a3.asInstanceOf[T3],
            a4.asInstanceOf[T4],
            a5.asInstanceOf[T5])
      }
    }

    def apply[T1, T2, T3, T4, T5, T6](f: (T1, T2, T3, T4, T5, T6) => Any)(implicit m1:Manifest[T1], m2:Manifest[T2], m3:Manifest[T3], m4:Manifest[T4], m5:Manifest[T5], m6:Manifest[T6]) {
      register(functionParams(f)) {
        case List(a1:AnyRef, a2:AnyRef, a3:AnyRef, a4:AnyRef, a5:AnyRef, a6:AnyRef) =>
          f(a1.asInstanceOf[T1],
            a2.asInstanceOf[T2],
            a3.asInstanceOf[T3],
            a4.asInstanceOf[T4],
            a5.asInstanceOf[T5],
            a6.asInstanceOf[T6])
      }
    }

    def apply[T1, T2, T3, T4, T5, T6, T7](f: (T1, T2, T3, T4, T5, T6, T7) => Any)(implicit m1:Manifest[T1], m2:Manifest[T2], m3:Manifest[T3], m4:Manifest[T4], m5:Manifest[T5], m6:Manifest[T6], m7:Manifest[T7]) {
      register(functionParams(f)) {
        case List(a1:AnyRef, a2:AnyRef, a3:AnyRef, a4:AnyRef, a5:AnyRef, a6:AnyRef, a7:AnyRef) =>
          f(a1.asInstanceOf[T1],
            a2.asInstanceOf[T2],
            a3.asInstanceOf[T3],
            a4.asInstanceOf[T4],
            a5.asInstanceOf[T5],
            a6.asInstanceOf[T6],
            a7.asInstanceOf[T7])
      }
    }

    def apply[T1, T2, T3, T4, T5, T6, T7, T8](f: (T1, T2, T3, T4, T5, T6, T7, T8) => Any)(implicit m1:Manifest[T1], m2:Manifest[T2], m3:Manifest[T3], m4:Manifest[T4], m5:Manifest[T5], m6:Manifest[T6], m7:Manifest[T7], m8:Manifest[T8]) {
      register(functionParams(f)) {
        case List(a1:AnyRef, a2:AnyRef, a3:AnyRef, a4:AnyRef, a5:AnyRef, a6:AnyRef, a7:AnyRef, a8:AnyRef) =>
          f(a1.asInstanceOf[T1],
            a2.asInstanceOf[T2],
            a3.asInstanceOf[T3],
            a4.asInstanceOf[T4],
            a5.asInstanceOf[T5],
            a6.asInstanceOf[T6],
            a7.asInstanceOf[T7],
            a8.asInstanceOf[T8])
      }
    }

    def apply[T1, T2, T3, T4, T5, T6, T7, T8, T9](f: (T1, T2, T3, T4, T5, T6, T7, T8, T9) => Any)(implicit m1:Manifest[T1], m2:Manifest[T2], m3:Manifest[T3], m4:Manifest[T4], m5:Manifest[T5], m6:Manifest[T6], m7:Manifest[T7], m8:Manifest[T8], m9:Manifest[T9]) {
      register(functionParams(f)) {
        case List(a1:AnyRef, a2:AnyRef, a3:AnyRef, a4:AnyRef, a5:AnyRef, a6:AnyRef, a7:AnyRef, a8:AnyRef, a9:AnyRef) =>
          f(a1.asInstanceOf[T1],
            a2.asInstanceOf[T2],
            a3.asInstanceOf[T3],
            a4.asInstanceOf[T4],
            a5.asInstanceOf[T5],
            a6.asInstanceOf[T6],
            a7.asInstanceOf[T7],
            a8.asInstanceOf[T8],
            a9.asInstanceOf[T9])
      }
    }

    def apply[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10](f: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10) => Any)(implicit m1:Manifest[T1], m2:Manifest[T2], m3:Manifest[T3], m4:Manifest[T4], m5:Manifest[T5], m6:Manifest[T6], m7:Manifest[T7], m8:Manifest[T8], m9:Manifest[T9], m10:Manifest[T10]) {
      register(functionParams(f)) {
        case List(a1:AnyRef, a2:AnyRef, a3:AnyRef, a4:AnyRef, a5:AnyRef, a6:AnyRef, a7:AnyRef, a8:AnyRef, a9:AnyRef, a10:AnyRef) =>
          f(a1.asInstanceOf[T1],
            a2.asInstanceOf[T2],
            a3.asInstanceOf[T3],
            a4.asInstanceOf[T4],
            a5.asInstanceOf[T5],
            a6.asInstanceOf[T6],
            a7.asInstanceOf[T7],
            a8.asInstanceOf[T8],
            a9.asInstanceOf[T9],
            a10.asInstanceOf[T10])
      }
    }

    def apply[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11](f: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11) => Any)(implicit m1:Manifest[T1], m2:Manifest[T2], m3:Manifest[T3], m4:Manifest[T4], m5:Manifest[T5], m6:Manifest[T6], m7:Manifest[T7], m8:Manifest[T8], m9:Manifest[T9], m10:Manifest[T10], m11:Manifest[T11]) {
      register(functionParams(f)) {
        case List(a1:AnyRef, a2:AnyRef, a3:AnyRef, a4:AnyRef, a5:AnyRef, a6:AnyRef, a7:AnyRef, a8:AnyRef, a9:AnyRef, a10:AnyRef, a11:AnyRef) =>
          f(a1.asInstanceOf[T1],
            a2.asInstanceOf[T2],
            a3.asInstanceOf[T3],
            a4.asInstanceOf[T4],
            a5.asInstanceOf[T5],
            a6.asInstanceOf[T6],
            a7.asInstanceOf[T7],
            a8.asInstanceOf[T8],
            a9.asInstanceOf[T9],
            a10.asInstanceOf[T10],
            a11.asInstanceOf[T11])
      }
    }

    def apply[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12](f: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12) => Any)(implicit m1:Manifest[T1], m2:Manifest[T2], m3:Manifest[T3], m4:Manifest[T4], m5:Manifest[T5], m6:Manifest[T6], m7:Manifest[T7], m8:Manifest[T8], m9:Manifest[T9], m10:Manifest[T10], m11:Manifest[T11], m12:Manifest[T12]) {
      register(functionParams(f)) {
        case List(a1:AnyRef, a2:AnyRef, a3:AnyRef, a4:AnyRef, a5:AnyRef, a6:AnyRef, a7:AnyRef, a8:AnyRef, a9:AnyRef, a10:AnyRef, a11:AnyRef, a12:AnyRef) =>
          f(a1.asInstanceOf[T1],
            a2.asInstanceOf[T2],
            a3.asInstanceOf[T3],
            a4.asInstanceOf[T4],
            a5.asInstanceOf[T5],
            a6.asInstanceOf[T6],
            a7.asInstanceOf[T7],
            a8.asInstanceOf[T8],
            a9.asInstanceOf[T9],
            a10.asInstanceOf[T10],
            a11.asInstanceOf[T11],
            a12.asInstanceOf[T12])
      }
    }

    def apply[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13](f: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13) => Any)(implicit m1:Manifest[T1], m2:Manifest[T2], m3:Manifest[T3], m4:Manifest[T4], m5:Manifest[T5], m6:Manifest[T6], m7:Manifest[T7], m8:Manifest[T8], m9:Manifest[T9], m10:Manifest[T10], m11:Manifest[T11], m12:Manifest[T12], m13:Manifest[T13]) {
      register(functionParams(f)) {
        case List(a1:AnyRef, a2:AnyRef, a3:AnyRef, a4:AnyRef, a5:AnyRef, a6:AnyRef, a7:AnyRef, a8:AnyRef, a9:AnyRef, a10:AnyRef, a11:AnyRef, a12:AnyRef, a13:AnyRef) =>
          f(a1.asInstanceOf[T1],
            a2.asInstanceOf[T2],
            a3.asInstanceOf[T3],
            a4.asInstanceOf[T4],
            a5.asInstanceOf[T5],
            a6.asInstanceOf[T6],
            a7.asInstanceOf[T7],
            a8.asInstanceOf[T8],
            a9.asInstanceOf[T9],
            a10.asInstanceOf[T10],
            a11.asInstanceOf[T11],
            a12.asInstanceOf[T12],
            a13.asInstanceOf[T13])
      }
    }

    def apply[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14](f: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14) => Any)(implicit m1:Manifest[T1], m2:Manifest[T2], m3:Manifest[T3], m4:Manifest[T4], m5:Manifest[T5], m6:Manifest[T6], m7:Manifest[T7], m8:Manifest[T8], m9:Manifest[T9], m10:Manifest[T10], m11:Manifest[T11], m12:Manifest[T12], m13:Manifest[T13], m14:Manifest[T14]) {
      register(functionParams(f)) {
        case List(a1:AnyRef, a2:AnyRef, a3:AnyRef, a4:AnyRef, a5:AnyRef, a6:AnyRef, a7:AnyRef, a8:AnyRef, a9:AnyRef, a10:AnyRef, a11:AnyRef, a12:AnyRef, a13:AnyRef, a14:AnyRef) =>
          f(a1.asInstanceOf[T1],
            a2.asInstanceOf[T2],
            a3.asInstanceOf[T3],
            a4.asInstanceOf[T4],
            a5.asInstanceOf[T5],
            a6.asInstanceOf[T6],
            a7.asInstanceOf[T7],
            a8.asInstanceOf[T8],
            a9.asInstanceOf[T9],
            a10.asInstanceOf[T10],
            a11.asInstanceOf[T11],
            a12.asInstanceOf[T12],
            a13.asInstanceOf[T13],
            a14.asInstanceOf[T14])
      }
    }

    def apply[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15](f: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15) => Any)(implicit m1:Manifest[T1], m2:Manifest[T2], m3:Manifest[T3], m4:Manifest[T4], m5:Manifest[T5], m6:Manifest[T6], m7:Manifest[T7], m8:Manifest[T8], m9:Manifest[T9], m10:Manifest[T10], m11:Manifest[T11], m12:Manifest[T12], m13:Manifest[T13], m14:Manifest[T14], m15:Manifest[T15]) {
      register(functionParams(f)) {
        case List(a1:AnyRef, a2:AnyRef, a3:AnyRef, a4:AnyRef, a5:AnyRef, a6:AnyRef, a7:AnyRef, a8:AnyRef, a9:AnyRef, a10:AnyRef, a11:AnyRef, a12:AnyRef, a13:AnyRef, a14:AnyRef, a15:AnyRef) =>
          f(a1.asInstanceOf[T1],
            a2.asInstanceOf[T2],
            a3.asInstanceOf[T3],
            a4.asInstanceOf[T4],
            a5.asInstanceOf[T5],
            a6.asInstanceOf[T6],
            a7.asInstanceOf[T7],
            a8.asInstanceOf[T8],
            a9.asInstanceOf[T9],
            a10.asInstanceOf[T10],
            a11.asInstanceOf[T11],
            a12.asInstanceOf[T12],
            a13.asInstanceOf[T13],
            a14.asInstanceOf[T14],
            a15.asInstanceOf[T15])
      }
    }

    def apply[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16](f: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16) => Any)(implicit m1:Manifest[T1], m2:Manifest[T2], m3:Manifest[T3], m4:Manifest[T4], m5:Manifest[T5], m6:Manifest[T6], m7:Manifest[T7], m8:Manifest[T8], m9:Manifest[T9], m10:Manifest[T10], m11:Manifest[T11], m12:Manifest[T12], m13:Manifest[T13], m14:Manifest[T14], m15:Manifest[T15], m16:Manifest[T16]) {
      register(functionParams(f)) {
        case List(a1:AnyRef, a2:AnyRef, a3:AnyRef, a4:AnyRef, a5:AnyRef, a6:AnyRef, a7:AnyRef, a8:AnyRef, a9:AnyRef, a10:AnyRef, a11:AnyRef, a12:AnyRef, a13:AnyRef, a14:AnyRef, a15:AnyRef, a16:AnyRef) =>
          f(a1.asInstanceOf[T1],
            a2.asInstanceOf[T2],
            a3.asInstanceOf[T3],
            a4.asInstanceOf[T4],
            a5.asInstanceOf[T5],
            a6.asInstanceOf[T6],
            a7.asInstanceOf[T7],
            a8.asInstanceOf[T8],
            a9.asInstanceOf[T9],
            a10.asInstanceOf[T10],
            a11.asInstanceOf[T11],
            a12.asInstanceOf[T12],
            a13.asInstanceOf[T13],
            a14.asInstanceOf[T14],
            a15.asInstanceOf[T15],
            a16.asInstanceOf[T16])
      }
    }

    def apply[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17](f: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17) => Any)(implicit m1:Manifest[T1], m2:Manifest[T2], m3:Manifest[T3], m4:Manifest[T4], m5:Manifest[T5], m6:Manifest[T6], m7:Manifest[T7], m8:Manifest[T8], m9:Manifest[T9], m10:Manifest[T10], m11:Manifest[T11], m12:Manifest[T12], m13:Manifest[T13], m14:Manifest[T14], m15:Manifest[T15], m16:Manifest[T16], m17:Manifest[T17]) {
      register(functionParams(f)) {
        case List(a1:AnyRef, a2:AnyRef, a3:AnyRef, a4:AnyRef, a5:AnyRef, a6:AnyRef, a7:AnyRef, a8:AnyRef, a9:AnyRef, a10:AnyRef, a11:AnyRef, a12:AnyRef, a13:AnyRef, a14:AnyRef, a15:AnyRef, a16:AnyRef, a17:AnyRef) =>
          f(a1.asInstanceOf[T1],
            a2.asInstanceOf[T2],
            a3.asInstanceOf[T3],
            a4.asInstanceOf[T4],
            a5.asInstanceOf[T5],
            a6.asInstanceOf[T6],
            a7.asInstanceOf[T7],
            a8.asInstanceOf[T8],
            a9.asInstanceOf[T9],
            a10.asInstanceOf[T10],
            a11.asInstanceOf[T11],
            a12.asInstanceOf[T12],
            a13.asInstanceOf[T13],
            a14.asInstanceOf[T14],
            a15.asInstanceOf[T15],
            a16.asInstanceOf[T16],
            a17.asInstanceOf[T17])
      }
    }

    def apply[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18](f: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18) => Any)(implicit m1:Manifest[T1], m2:Manifest[T2], m3:Manifest[T3], m4:Manifest[T4], m5:Manifest[T5], m6:Manifest[T6], m7:Manifest[T7], m8:Manifest[T8], m9:Manifest[T9], m10:Manifest[T10], m11:Manifest[T11], m12:Manifest[T12], m13:Manifest[T13], m14:Manifest[T14], m15:Manifest[T15], m16:Manifest[T16], m17:Manifest[T17], m18:Manifest[T18]) {
      register(functionParams(f)) {
        case List(a1:AnyRef, a2:AnyRef, a3:AnyRef, a4:AnyRef, a5:AnyRef, a6:AnyRef, a7:AnyRef, a8:AnyRef, a9:AnyRef, a10:AnyRef, a11:AnyRef, a12:AnyRef, a13:AnyRef, a14:AnyRef, a15:AnyRef, a16:AnyRef, a17:AnyRef, a18:AnyRef) =>
          f(a1.asInstanceOf[T1],
            a2.asInstanceOf[T2],
            a3.asInstanceOf[T3],
            a4.asInstanceOf[T4],
            a5.asInstanceOf[T5],
            a6.asInstanceOf[T6],
            a7.asInstanceOf[T7],
            a8.asInstanceOf[T8],
            a9.asInstanceOf[T9],
            a10.asInstanceOf[T10],
            a11.asInstanceOf[T11],
            a12.asInstanceOf[T12],
            a13.asInstanceOf[T13],
            a14.asInstanceOf[T14],
            a15.asInstanceOf[T15],
            a16.asInstanceOf[T16],
            a17.asInstanceOf[T17],
            a18.asInstanceOf[T18])
      }
    }

    def apply[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19](f: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19) => Any)(implicit m1:Manifest[T1], m2:Manifest[T2], m3:Manifest[T3], m4:Manifest[T4], m5:Manifest[T5], m6:Manifest[T6], m7:Manifest[T7], m8:Manifest[T8], m9:Manifest[T9], m10:Manifest[T10], m11:Manifest[T11], m12:Manifest[T12], m13:Manifest[T13], m14:Manifest[T14], m15:Manifest[T15], m16:Manifest[T16], m17:Manifest[T17], m18:Manifest[T18], m19:Manifest[T19]) {
      register(functionParams(f)) {
        case List(a1:AnyRef, a2:AnyRef, a3:AnyRef, a4:AnyRef, a5:AnyRef, a6:AnyRef, a7:AnyRef, a8:AnyRef, a9:AnyRef, a10:AnyRef, a11:AnyRef, a12:AnyRef, a13:AnyRef, a14:AnyRef, a15:AnyRef, a16:AnyRef, a17:AnyRef, a18:AnyRef, a19:AnyRef) =>
          f(a1.asInstanceOf[T1],
            a2.asInstanceOf[T2],
            a3.asInstanceOf[T3],
            a4.asInstanceOf[T4],
            a5.asInstanceOf[T5],
            a6.asInstanceOf[T6],
            a7.asInstanceOf[T7],
            a8.asInstanceOf[T8],
            a9.asInstanceOf[T9],
            a10.asInstanceOf[T10],
            a11.asInstanceOf[T11],
            a12.asInstanceOf[T12],
            a13.asInstanceOf[T13],
            a14.asInstanceOf[T14],
            a15.asInstanceOf[T15],
            a16.asInstanceOf[T16],
            a17.asInstanceOf[T17],
            a18.asInstanceOf[T18],
            a19.asInstanceOf[T19])
      }
    }

    def apply[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20](f: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20) => Any)(implicit m1:Manifest[T1], m2:Manifest[T2], m3:Manifest[T3], m4:Manifest[T4], m5:Manifest[T5], m6:Manifest[T6], m7:Manifest[T7], m8:Manifest[T8], m9:Manifest[T9], m10:Manifest[T10], m11:Manifest[T11], m12:Manifest[T12], m13:Manifest[T13], m14:Manifest[T14], m15:Manifest[T15], m16:Manifest[T16], m17:Manifest[T17], m18:Manifest[T18], m19:Manifest[T19], m20:Manifest[T20]) {
      register(functionParams(f)) {
        case List(a1:AnyRef, a2:AnyRef, a3:AnyRef, a4:AnyRef, a5:AnyRef, a6:AnyRef, a7:AnyRef, a8:AnyRef, a9:AnyRef, a10:AnyRef, a11:AnyRef, a12:AnyRef, a13:AnyRef, a14:AnyRef, a15:AnyRef, a16:AnyRef, a17:AnyRef, a18:AnyRef, a19:AnyRef, a20:AnyRef) =>
          f(a1.asInstanceOf[T1],
            a2.asInstanceOf[T2],
            a3.asInstanceOf[T3],
            a4.asInstanceOf[T4],
            a5.asInstanceOf[T5],
            a6.asInstanceOf[T6],
            a7.asInstanceOf[T7],
            a8.asInstanceOf[T8],
            a9.asInstanceOf[T9],
            a10.asInstanceOf[T10],
            a11.asInstanceOf[T11],
            a12.asInstanceOf[T12],
            a13.asInstanceOf[T13],
            a14.asInstanceOf[T14],
            a15.asInstanceOf[T15],
            a16.asInstanceOf[T16],
            a17.asInstanceOf[T17],
            a18.asInstanceOf[T18],
            a19.asInstanceOf[T19],
            a20.asInstanceOf[T20])
      }
    }

    def apply[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21](f: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21) => Any)(implicit m1:Manifest[T1], m2:Manifest[T2], m3:Manifest[T3], m4:Manifest[T4], m5:Manifest[T5], m6:Manifest[T6], m7:Manifest[T7], m8:Manifest[T8], m9:Manifest[T9], m10:Manifest[T10], m11:Manifest[T11], m12:Manifest[T12], m13:Manifest[T13], m14:Manifest[T14], m15:Manifest[T15], m16:Manifest[T16], m17:Manifest[T17], m18:Manifest[T18], m19:Manifest[T19], m20:Manifest[T20], m21:Manifest[T21]) {
      register(functionParams(f)) {
        case List(a1:AnyRef, a2:AnyRef, a3:AnyRef, a4:AnyRef, a5:AnyRef, a6:AnyRef, a7:AnyRef, a8:AnyRef, a9:AnyRef, a10:AnyRef, a11:AnyRef, a12:AnyRef, a13:AnyRef, a14:AnyRef, a15:AnyRef, a16:AnyRef, a17:AnyRef, a18:AnyRef, a19:AnyRef, a20:AnyRef, a21:AnyRef) =>
          f(a1.asInstanceOf[T1],
            a2.asInstanceOf[T2],
            a3.asInstanceOf[T3],
            a4.asInstanceOf[T4],
            a5.asInstanceOf[T5],
            a6.asInstanceOf[T6],
            a7.asInstanceOf[T7],
            a8.asInstanceOf[T8],
            a9.asInstanceOf[T9],
            a10.asInstanceOf[T10],
            a11.asInstanceOf[T11],
            a12.asInstanceOf[T12],
            a13.asInstanceOf[T13],
            a14.asInstanceOf[T14],
            a15.asInstanceOf[T15],
            a16.asInstanceOf[T16],
            a17.asInstanceOf[T17],
            a18.asInstanceOf[T18],
            a19.asInstanceOf[T19],
            a20.asInstanceOf[T20],
            a21.asInstanceOf[T21])
      }
    }

    def apply[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22](f: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22) => Any)(implicit m1:Manifest[T1], m2:Manifest[T2], m3:Manifest[T3], m4:Manifest[T4], m5:Manifest[T5], m6:Manifest[T6], m7:Manifest[T7], m8:Manifest[T8], m9:Manifest[T9], m10:Manifest[T10], m11:Manifest[T11], m12:Manifest[T12], m13:Manifest[T13], m14:Manifest[T14], m15:Manifest[T15], m16:Manifest[T16], m17:Manifest[T17], m18:Manifest[T18], m19:Manifest[T19], m20:Manifest[T20], m21:Manifest[T21], m22:Manifest[T22]) {
      register(functionParams(f)) {
        case List(a1:AnyRef, a2:AnyRef, a3:AnyRef, a4:AnyRef, a5:AnyRef, a6:AnyRef, a7:AnyRef, a8:AnyRef, a9:AnyRef, a10:AnyRef, a11:AnyRef, a12:AnyRef, a13:AnyRef, a14:AnyRef, a15:AnyRef, a16:AnyRef, a17:AnyRef, a18:AnyRef, a19:AnyRef, a20:AnyRef, a21:AnyRef, a22:AnyRef) =>
          f(a1.asInstanceOf[T1],
            a2.asInstanceOf[T2],
            a3.asInstanceOf[T3],
            a4.asInstanceOf[T4],
            a5.asInstanceOf[T5],
            a6.asInstanceOf[T6],
            a7.asInstanceOf[T7],
            a8.asInstanceOf[T8],
            a9.asInstanceOf[T9],
            a10.asInstanceOf[T10],
            a11.asInstanceOf[T11],
            a12.asInstanceOf[T12],
            a13.asInstanceOf[T13],
            a14.asInstanceOf[T14],
            a15.asInstanceOf[T15],
            a16.asInstanceOf[T16],
            a17.asInstanceOf[T17],
            a18.asInstanceOf[T18],
            a19.asInstanceOf[T19],
            a20.asInstanceOf[T20],
            a21.asInstanceOf[T21],
            a22.asInstanceOf[T22])
      }
    }

    private def register(types: Array[Type])(pf: PartialFunction[List[Any], Any]) = {
      val frame: StackTraceElement = obtainFrame
      stepDefinitions += new ScalaStepDefinition(frame, name, regex, types, pf)
    }

    /**
     * Return the stack frame to allow us to identify where in a step definition file
     * we are currently based
     */
    private def obtainFrame: StackTraceElement = {
      val frames = Thread.currentThread().getStackTrace
      val currentClass = self.getClass.getName
      frames.find(_.getClassName == currentClass).get
    }

    private def functionParams(f: Any) =
      f.getClass.getDeclaredMethods.filter(m => "apply".equals(m.getName) && !m.isBridge).head.getGenericParameterTypes
  }
}
