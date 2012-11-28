package cucumber.api.scala

import _root_.cucumber.api.Scenario
import _root_.cucumber.runtime.scala.Transform
import _root_.cucumber.runtime.scala.ScalaHookDefinition
import _root_.cucumber.runtime.scala.ScalaStepDefinition
import _root_.cucumber.runtime.HookDefinition
import _root_.cucumber.runtime.StepDefinition
import collection.mutable.ArrayBuffer

trait ScalaDsl { self =>

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
    //treat call-by-name like a Fun of Function0
    def apply(f: => Unit){ apply(f _) }
    //get around type erasure where call by name has same erasure as Function0
    def apply(fun: Fun0) = register(Nil) {case Nil => fun.f()}

    def apply[T1](f: (T1) => Any)(implicit m1: Manifest[T1], t1: Transform[T1]) = register(List(m1)) {case List(a1) => f(t1(a1))}

    def apply[T1, T2](f: (T1, T2) => Any)(implicit m1: Manifest[T1], t1: Transform[T1], m2: Manifest[T2], t2: Transform[T2]) = register(List(m1, m2)) {case List(a1, a2) => f(t1(a1), t2(a2))}

    def apply[T1, T2, T3](f: (T1, T2, T3) => Any)(implicit m1: Manifest[T1], t1: Transform[T1], m2: Manifest[T2], t2: Transform[T2], m3: Manifest[T3], t3: Transform[T3]) = register(List(m1, m2, m3)) {case List(a1, a2, a3) => f(t1(a1), t2(a2), t3(a3))}

    def apply[T1, T2, T3, T4](f: (T1, T2, T3, T4) => Any)(implicit m1: Manifest[T1], t1: Transform[T1], m2: Manifest[T2], t2: Transform[T2], m3: Manifest[T3], t3: Transform[T3], m4: Manifest[T4], t4: Transform[T4]) = register(List(m1, m2, m3, m4)) {case List(a1, a2, a3, a4) => f(t1(a1), t2(a2), t3(a3), t4(a4))}

    def apply[T1, T2, T3, T4, T5](f: (T1, T2, T3, T4, T5) => Any)(implicit m1: Manifest[T1], t1: Transform[T1], m2: Manifest[T2], t2: Transform[T2], m3: Manifest[T3], t3: Transform[T3], m4: Manifest[T4], t4: Transform[T4], m5: Manifest[T5], t5: Transform[T5]) = register(List(m1, m2, m3, m4, m5)) {case List(a1, a2, a3, a4, a5) => f(t1(a1), t2(a2), t3(a3), t4(a4), t5(a5))}

    def apply[T1, T2, T3, T4, T5, T6](f: (T1, T2, T3, T4, T5, T6) => Any)(implicit m1: Manifest[T1], t1: Transform[T1], m2: Manifest[T2], t2: Transform[T2], m3: Manifest[T3], t3: Transform[T3], m4: Manifest[T4], t4: Transform[T4], m5: Manifest[T5], t5: Transform[T5], m6: Manifest[T6], t6: Transform[T6]) = register(List(m1, m2, m3, m4, m5, m6)) {case List(a1, a2, a3, a4, a5, a6) => f(t1(a1), t2(a2), t3(a3), t4(a4), t5(a5), t6(a6))}

    def apply[T1, T2, T3, T4, T5, T6, T7](f: (T1, T2, T3, T4, T5, T6, T7) => Any)(implicit m1: Manifest[T1], t1: Transform[T1], m2: Manifest[T2], t2: Transform[T2], m3: Manifest[T3], t3: Transform[T3], m4: Manifest[T4], t4: Transform[T4], m5: Manifest[T5], t5: Transform[T5], m6: Manifest[T6], t6: Transform[T6], m7: Manifest[T7], t7: Transform[T7]) = register(List(m1, m2, m3, m4, m5, m6, m7)) {case List(a1, a2, a3, a4, a5, a6, a7) => f(t1(a1), t2(a2), t3(a3), t4(a4), t5(a5), t6(a6), t7(a7))}

    def apply[T1, T2, T3, T4, T5, T6, T7, T8](f: (T1, T2, T3, T4, T5, T6, T7, T8) => Any)(implicit m1: Manifest[T1], t1: Transform[T1], m2: Manifest[T2], t2: Transform[T2], m3: Manifest[T3], t3: Transform[T3], m4: Manifest[T4], t4: Transform[T4], m5: Manifest[T5], t5: Transform[T5], m6: Manifest[T6], t6: Transform[T6], m7: Manifest[T7], t7: Transform[T7], m8: Manifest[T8], t8: Transform[T8]) = register(List(m1, m2, m3, m4, m5, m6, m7, m8)) {case List(a1, a2, a3, a4, a5, a6, a7, a8) => f(t1(a1), t2(a2), t3(a3), t4(a4), t5(a5), t6(a6), t7(a7), t8(a8))}

    def apply[T1, T2, T3, T4, T5, T6, T7, T8, T9](f: (T1, T2, T3, T4, T5, T6, T7, T8, T9) => Any)(implicit m1: Manifest[T1], t1: Transform[T1], m2: Manifest[T2], t2: Transform[T2], m3: Manifest[T3], t3: Transform[T3], m4: Manifest[T4], t4: Transform[T4], m5: Manifest[T5], t5: Transform[T5], m6: Manifest[T6], t6: Transform[T6], m7: Manifest[T7], t7: Transform[T7], m8: Manifest[T8], t8: Transform[T8], m9: Manifest[T9], t9: Transform[T9]) = register(List(m1, m2, m3, m4, m5, m6, m7, m8, m9)) {case List(a1, a2, a3, a4, a5, a6, a7, a8, a9) => f(t1(a1), t2(a2), t3(a3), t4(a4), t5(a5), t6(a6), t7(a7), t8(a8), t9(a9))}

    def apply[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10](f: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10) => Any)(implicit m1: Manifest[T1], t1: Transform[T1], m2: Manifest[T2], t2: Transform[T2], m3: Manifest[T3], t3: Transform[T3], m4: Manifest[T4], t4: Transform[T4], m5: Manifest[T5], t5: Transform[T5], m6: Manifest[T6], t6: Transform[T6], m7: Manifest[T7], t7: Transform[T7], m8: Manifest[T8], t8: Transform[T8], m9: Manifest[T9], t9: Transform[T9], m10: Manifest[T10], t10: Transform[T10]) = register(List(m1, m2, m3, m4, m5, m6, m7, m8, m9, m10)) {case List(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10) => f(t1(a1), t2(a2), t3(a3), t4(a4), t5(a5), t6(a6), t7(a7), t8(a8), t9(a9), t10(a10))}

    def apply[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11](f: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11) => Any)(implicit m1: Manifest[T1], t1: Transform[T1], m2: Manifest[T2], t2: Transform[T2], m3: Manifest[T3], t3: Transform[T3], m4: Manifest[T4], t4: Transform[T4], m5: Manifest[T5], t5: Transform[T5], m6: Manifest[T6], t6: Transform[T6], m7: Manifest[T7], t7: Transform[T7], m8: Manifest[T8], t8: Transform[T8], m9: Manifest[T9], t9: Transform[T9], m10: Manifest[T10], t10: Transform[T10], m11: Manifest[T11], t11: Transform[T11]) = register(List(m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, m11)) {case List(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11) => f(t1(a1), t2(a2), t3(a3), t4(a4), t5(a5), t6(a6), t7(a7), t8(a8), t9(a9), t10(a10), t11(a11))}

    def apply[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12](f: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12) => Any)(implicit m1: Manifest[T1], t1: Transform[T1], m2: Manifest[T2], t2: Transform[T2], m3: Manifest[T3], t3: Transform[T3], m4: Manifest[T4], t4: Transform[T4], m5: Manifest[T5], t5: Transform[T5], m6: Manifest[T6], t6: Transform[T6], m7: Manifest[T7], t7: Transform[T7], m8: Manifest[T8], t8: Transform[T8], m9: Manifest[T9], t9: Transform[T9], m10: Manifest[T10], t10: Transform[T10], m11: Manifest[T11], t11: Transform[T11], m12: Manifest[T12], t12: Transform[T12]) = register(List(m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, m11, m12)) {case List(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12) => f(t1(a1), t2(a2), t3(a3), t4(a4), t5(a5), t6(a6), t7(a7), t8(a8), t9(a9), t10(a10), t11(a11), t12(a12))}

    def apply[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13](f: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13) => Any)(implicit m1: Manifest[T1], t1: Transform[T1], m2: Manifest[T2], t2: Transform[T2], m3: Manifest[T3], t3: Transform[T3], m4: Manifest[T4], t4: Transform[T4], m5: Manifest[T5], t5: Transform[T5], m6: Manifest[T6], t6: Transform[T6], m7: Manifest[T7], t7: Transform[T7], m8: Manifest[T8], t8: Transform[T8], m9: Manifest[T9], t9: Transform[T9], m10: Manifest[T10], t10: Transform[T10], m11: Manifest[T11], t11: Transform[T11], m12: Manifest[T12], t12: Transform[T12], m13: Manifest[T13], t13: Transform[T13]) = register(List(m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, m11, m12, m13)) {case List(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13) => f(t1(a1), t2(a2), t3(a3), t4(a4), t5(a5), t6(a6), t7(a7), t8(a8), t9(a9), t10(a10), t11(a11), t12(a12), t13(a13))}

    def apply[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14](f: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14) => Any)(implicit m1: Manifest[T1], t1: Transform[T1], m2: Manifest[T2], t2: Transform[T2], m3: Manifest[T3], t3: Transform[T3], m4: Manifest[T4], t4: Transform[T4], m5: Manifest[T5], t5: Transform[T5], m6: Manifest[T6], t6: Transform[T6], m7: Manifest[T7], t7: Transform[T7], m8: Manifest[T8], t8: Transform[T8], m9: Manifest[T9], t9: Transform[T9], m10: Manifest[T10], t10: Transform[T10], m11: Manifest[T11], t11: Transform[T11], m12: Manifest[T12], t12: Transform[T12], m13: Manifest[T13], t13: Transform[T13], m14: Manifest[T14], t14: Transform[T14]) = register(List(m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, m11, m12, m13, m14)) {case List(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13, a14) => f(t1(a1), t2(a2), t3(a3), t4(a4), t5(a5), t6(a6), t7(a7), t8(a8), t9(a9), t10(a10), t11(a11), t12(a12), t13(a13), t14(a14))}

    def apply[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15](f: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15) => Any)(implicit m1: Manifest[T1], t1: Transform[T1], m2: Manifest[T2], t2: Transform[T2], m3: Manifest[T3], t3: Transform[T3], m4: Manifest[T4], t4: Transform[T4], m5: Manifest[T5], t5: Transform[T5], m6: Manifest[T6], t6: Transform[T6], m7: Manifest[T7], t7: Transform[T7], m8: Manifest[T8], t8: Transform[T8], m9: Manifest[T9], t9: Transform[T9], m10: Manifest[T10], t10: Transform[T10], m11: Manifest[T11], t11: Transform[T11], m12: Manifest[T12], t12: Transform[T12], m13: Manifest[T13], t13: Transform[T13], m14: Manifest[T14], t14: Transform[T14], m15: Manifest[T15], t15: Transform[T15]) = register(List(m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, m11, m12, m13, m14, m15)) {case List(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13, a14, a15) => f(t1(a1), t2(a2), t3(a3), t4(a4), t5(a5), t6(a6), t7(a7), t8(a8), t9(a9), t10(a10), t11(a11), t12(a12), t13(a13), t14(a14), t15(a15))}

    def apply[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16](f: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16) => Any)(implicit m1: Manifest[T1], t1: Transform[T1], m2: Manifest[T2], t2: Transform[T2], m3: Manifest[T3], t3: Transform[T3], m4: Manifest[T4], t4: Transform[T4], m5: Manifest[T5], t5: Transform[T5], m6: Manifest[T6], t6: Transform[T6], m7: Manifest[T7], t7: Transform[T7], m8: Manifest[T8], t8: Transform[T8], m9: Manifest[T9], t9: Transform[T9], m10: Manifest[T10], t10: Transform[T10], m11: Manifest[T11], t11: Transform[T11], m12: Manifest[T12], t12: Transform[T12], m13: Manifest[T13], t13: Transform[T13], m14: Manifest[T14], t14: Transform[T14], m15: Manifest[T15], t15: Transform[T15], m16: Manifest[T16], t16: Transform[T16]) = register(List(m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, m11, m12, m13, m14, m15, m16)) {case List(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13, a14, a15, a16) => f(t1(a1), t2(a2), t3(a3), t4(a4), t5(a5), t6(a6), t7(a7), t8(a8), t9(a9), t10(a10), t11(a11), t12(a12), t13(a13), t14(a14), t15(a15), t16(a16))}

    def apply[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17](f: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17) => Any)(implicit m1: Manifest[T1], t1: Transform[T1], m2: Manifest[T2], t2: Transform[T2], m3: Manifest[T3], t3: Transform[T3], m4: Manifest[T4], t4: Transform[T4], m5: Manifest[T5], t5: Transform[T5], m6: Manifest[T6], t6: Transform[T6], m7: Manifest[T7], t7: Transform[T7], m8: Manifest[T8], t8: Transform[T8], m9: Manifest[T9], t9: Transform[T9], m10: Manifest[T10], t10: Transform[T10], m11: Manifest[T11], t11: Transform[T11], m12: Manifest[T12], t12: Transform[T12], m13: Manifest[T13], t13: Transform[T13], m14: Manifest[T14], t14: Transform[T14], m15: Manifest[T15], t15: Transform[T15], m16: Manifest[T16], t16: Transform[T16], m17: Manifest[T17], t17: Transform[T17]) = register(List(m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, m11, m12, m13, m14, m15, m16, m17)) {case List(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13, a14, a15, a16, a17) => f(t1(a1), t2(a2), t3(a3), t4(a4), t5(a5), t6(a6), t7(a7), t8(a8), t9(a9), t10(a10), t11(a11), t12(a12), t13(a13), t14(a14), t15(a15), t16(a16), t17(a17))}

    def apply[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18](f: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18) => Any)(implicit m1: Manifest[T1], t1: Transform[T1], m2: Manifest[T2], t2: Transform[T2], m3: Manifest[T3], t3: Transform[T3], m4: Manifest[T4], t4: Transform[T4], m5: Manifest[T5], t5: Transform[T5], m6: Manifest[T6], t6: Transform[T6], m7: Manifest[T7], t7: Transform[T7], m8: Manifest[T8], t8: Transform[T8], m9: Manifest[T9], t9: Transform[T9], m10: Manifest[T10], t10: Transform[T10], m11: Manifest[T11], t11: Transform[T11], m12: Manifest[T12], t12: Transform[T12], m13: Manifest[T13], t13: Transform[T13], m14: Manifest[T14], t14: Transform[T14], m15: Manifest[T15], t15: Transform[T15], m16: Manifest[T16], t16: Transform[T16], m17: Manifest[T17], t17: Transform[T17], m18: Manifest[T18], t18: Transform[T18]) = register(List(m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, m11, m12, m13, m14, m15, m16, m17, m18)) {case List(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13, a14, a15, a16, a17, a18) => f(t1(a1), t2(a2), t3(a3), t4(a4), t5(a5), t6(a6), t7(a7), t8(a8), t9(a9), t10(a10), t11(a11), t12(a12), t13(a13), t14(a14), t15(a15), t16(a16), t17(a17), t18(a18))}

    def apply[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19](f: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19) => Any)(implicit m1: Manifest[T1], t1: Transform[T1], m2: Manifest[T2], t2: Transform[T2], m3: Manifest[T3], t3: Transform[T3], m4: Manifest[T4], t4: Transform[T4], m5: Manifest[T5], t5: Transform[T5], m6: Manifest[T6], t6: Transform[T6], m7: Manifest[T7], t7: Transform[T7], m8: Manifest[T8], t8: Transform[T8], m9: Manifest[T9], t9: Transform[T9], m10: Manifest[T10], t10: Transform[T10], m11: Manifest[T11], t11: Transform[T11], m12: Manifest[T12], t12: Transform[T12], m13: Manifest[T13], t13: Transform[T13], m14: Manifest[T14], t14: Transform[T14], m15: Manifest[T15], t15: Transform[T15], m16: Manifest[T16], t16: Transform[T16], m17: Manifest[T17], t17: Transform[T17], m18: Manifest[T18], t18: Transform[T18], m19: Manifest[T19], t19: Transform[T19]) = register(List(m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, m11, m12, m13, m14, m15, m16, m17, m18, m19)) {case List(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13, a14, a15, a16, a17, a18, a19) => f(t1(a1), t2(a2), t3(a3), t4(a4), t5(a5), t6(a6), t7(a7), t8(a8), t9(a9), t10(a10), t11(a11), t12(a12), t13(a13), t14(a14), t15(a15), t16(a16), t17(a17), t18(a18), t19(a19))}

    def apply[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20](f: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20) => Any)(implicit m1: Manifest[T1], t1: Transform[T1], m2: Manifest[T2], t2: Transform[T2], m3: Manifest[T3], t3: Transform[T3], m4: Manifest[T4], t4: Transform[T4], m5: Manifest[T5], t5: Transform[T5], m6: Manifest[T6], t6: Transform[T6], m7: Manifest[T7], t7: Transform[T7], m8: Manifest[T8], t8: Transform[T8], m9: Manifest[T9], t9: Transform[T9], m10: Manifest[T10], t10: Transform[T10], m11: Manifest[T11], t11: Transform[T11], m12: Manifest[T12], t12: Transform[T12], m13: Manifest[T13], t13: Transform[T13], m14: Manifest[T14], t14: Transform[T14], m15: Manifest[T15], t15: Transform[T15], m16: Manifest[T16], t16: Transform[T16], m17: Manifest[T17], t17: Transform[T17], m18: Manifest[T18], t18: Transform[T18], m19: Manifest[T19], t19: Transform[T19], m20: Manifest[T20], t20: Transform[T20]) = register(List(m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, m11, m12, m13, m14, m15, m16, m17, m18, m19, m20)) {case List(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13, a14, a15, a16, a17, a18, a19, a20) => f(t1(a1), t2(a2), t3(a3), t4(a4), t5(a5), t6(a6), t7(a7), t8(a8), t9(a9), t10(a10), t11(a11), t12(a12), t13(a13), t14(a14), t15(a15), t16(a16), t17(a17), t18(a18), t19(a19), t20(a20))}

    def apply[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21](f: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21) => Any)(implicit m1: Manifest[T1], t1: Transform[T1], m2: Manifest[T2], t2: Transform[T2], m3: Manifest[T3], t3: Transform[T3], m4: Manifest[T4], t4: Transform[T4], m5: Manifest[T5], t5: Transform[T5], m6: Manifest[T6], t6: Transform[T6], m7: Manifest[T7], t7: Transform[T7], m8: Manifest[T8], t8: Transform[T8], m9: Manifest[T9], t9: Transform[T9], m10: Manifest[T10], t10: Transform[T10], m11: Manifest[T11], t11: Transform[T11], m12: Manifest[T12], t12: Transform[T12], m13: Manifest[T13], t13: Transform[T13], m14: Manifest[T14], t14: Transform[T14], m15: Manifest[T15], t15: Transform[T15], m16: Manifest[T16], t16: Transform[T16], m17: Manifest[T17], t17: Transform[T17], m18: Manifest[T18], t18: Transform[T18], m19: Manifest[T19], t19: Transform[T19], m20: Manifest[T20], t20: Transform[T20], m21: Manifest[T21], t21: Transform[T21]) = register(List(m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, m11, m12, m13, m14, m15, m16, m17, m18, m19, m20, m21)) {case List(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13, a14, a15, a16, a17, a18, a19, a20, a21) => f(t1(a1), t2(a2), t3(a3), t4(a4), t5(a5), t6(a6), t7(a7), t8(a8), t9(a9), t10(a10), t11(a11), t12(a12), t13(a13), t14(a14), t15(a15), t16(a16), t17(a17), t18(a18), t19(a19), t20(a20), t21(a21))}

    def apply[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22](f: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22) => Any)(implicit m1: Manifest[T1], t1: Transform[T1], m2: Manifest[T2], t2: Transform[T2], m3: Manifest[T3], t3: Transform[T3], m4: Manifest[T4], t4: Transform[T4], m5: Manifest[T5], t5: Transform[T5], m6: Manifest[T6], t6: Transform[T6], m7: Manifest[T7], t7: Transform[T7], m8: Manifest[T8], t8: Transform[T8], m9: Manifest[T9], t9: Transform[T9], m10: Manifest[T10], t10: Transform[T10], m11: Manifest[T11], t11: Transform[T11], m12: Manifest[T12], t12: Transform[T12], m13: Manifest[T13], t13: Transform[T13], m14: Manifest[T14], t14: Transform[T14], m15: Manifest[T15], t15: Transform[T15], m16: Manifest[T16], t16: Transform[T16], m17: Manifest[T17], t17: Transform[T17], m18: Manifest[T18], t18: Transform[T18], m19: Manifest[T19], t19: Transform[T19], m20: Manifest[T20], t20: Transform[T20], m21: Manifest[T21], t21: Transform[T21], m22: Manifest[T22], t22: Transform[T22]) = register(List(m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, m11, m12, m13, m14, m15, m16, m17, m18, m19, m20, m21, m22)) {case List(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13, a14, a15, a16, a17, a18, a19, a20, a21, a22) => f(t1(a1), t2(a2), t3(a3), t4(a4), t5(a5), t6(a6), t7(a7), t8(a8), t9(a9), t10(a10), t11(a11), t12(a12), t13(a13), t14(a14), t15(a15), t16(a16), t17(a17), t18(a18), t19(a19), t20(a20), t21(a21), t22(a22))}

    private def register(manifests: List[Manifest[_]])(pf: PartialFunction[List[Any], Any]){
      val frames = Thread.currentThread().getStackTrace
      val currentClass = self.getClass.getName
      val frame = frames.find(_.getClassName == currentClass).get
      stepDefinitions += new ScalaStepDefinition(frame, name, regex, manifests.map(_.erasure), pf)
    }
  }
}