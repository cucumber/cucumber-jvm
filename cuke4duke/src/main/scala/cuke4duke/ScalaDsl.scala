package cuke4duke

import _root_.scala.collection.mutable.ListBuffer
import _root_.scala.reflect.Manifest
import cuke4duke.internal.JRuby
import cuke4duke.internal.scala._
import cuke4duke.internal.language.AbstractProgrammingLanguage
/*
  <yourclass> {extends|with} ScalaDsl with EN
 */
trait ScalaDsl {

  private[cuke4duke] val stepDefinitions = new ListBuffer[AbstractProgrammingLanguage => ScalaStepDefinition]
  private[cuke4duke] val beforeHooks = new ListBuffer[ScalaHook]
  private[cuke4duke] val afterHooks = new ListBuffer[ScalaHook]

  /*
   * Adds a Hook to be run before every scenario
   */
  def Before(f: => Unit) = beforeHooks += new ScalaHook(Array(), f _)

  /*
   * Adds a Hook to be run before every scenario tagged with one of the given tags
   */
  def Before(tags: String*)(f: => Unit) = beforeHooks += new ScalaHook(tags.toArray, f _)

  /*
   * Adds a Hook to be run before every scenario
   */
  def After(f: => Unit) = afterHooks += new ScalaHook(Array(), f _)

  /*
   * Adds a Hook to be run before every scenario tagged with one of the given tags
   */
  def After(tags: String*)(f: => Unit) = afterHooks += new ScalaHook(tags.toArray, f _)

  /*
   * Marks the given feature as pending with the given message
   */
  def pending(message: String) { throw JRuby.cucumberPending(message) }

  /*
   * Marks the given feature as pending with the default message "TODO"
   */
  def pending { pending("TODO") }

  /*
   * Suspends execution and asks for input
   */
  def ask(question:String, timeout:Int) = mode.ask(question, timeout)

  /*
   * Output a message alongside the formatted output.
   * This is an alternative to using "println" - it will display
   * nicer, and in all outputs (in case you use several formatters)
   */
  def announce(message:String) = mode.announce(message)

  /*
   * Embed a file in the formatter outputs. This may or may
   * not be ignored, depending on what kind of formatter(s) are active.
   */
  def embed(file:String, mimeType:String) = mode.embed(file, mimeType)

  /*
   * see i18n.scala for instances
   */
  final class Step(name: String) {
    def apply(regex: String): StepBody = mode.createHandle(name, regex)

    def apply(regex: String, py: String): Unit = mode.callPy(name, regex, py)

    def apply(regex: String, table: Table): Unit = mode.callTable(name, regex, table)
  }

  class Fun0(val f:Function0[Any])

  object Fun0{
    implicit def function02Fun0(f:Function0[Any]) = new Fun0(f)
    implicit def handle2Fun0(h:StepBody) = new Fun0(() => h)
  }

  sealed trait StepBody {
    //treat call-by-name like a Fun of Function0
    def apply(f: => Unit):Unit = apply(f _)
    //get around type erasure where call by name has same erasure as Function0
    def apply(fun:Fun0) = doHandle(Nil){ case Nil => fun.f() }

    def apply[T1](f: (T1) => Any)(implicit m1:Manifest[T1], t1:Transformation[T1]) = doHandle(List(m1)){ case List(a1) => f(t1(a1)) }
    def apply[T1, T2](f: (T1, T2) => Any)(implicit m1:Manifest[T1], t1:Transformation[T1], m2:Manifest[T2], t2:Transformation[T2]) = doHandle(List(m1, m2)){ case List(a1, a2) => f(t1(a1), t2(a2)) }
    def apply[T1, T2, T3](f: (T1, T2, T3) => Any)(implicit m1:Manifest[T1], t1:Transformation[T1], m2:Manifest[T2], t2:Transformation[T2], m3:Manifest[T3], t3:Transformation[T3]) = doHandle(List(m1, m2, m3)){ case List(a1, a2, a3) => f(t1(a1), t2(a2), t3(a3)) }
    def apply[T1, T2, T3, T4](f: (T1, T2, T3, T4) => Any)(implicit m1:Manifest[T1], t1:Transformation[T1], m2:Manifest[T2], t2:Transformation[T2], m3:Manifest[T3], t3:Transformation[T3], m4:Manifest[T4], t4:Transformation[T4]) = doHandle(List(m1, m2, m3, m4)){ case List(a1, a2, a3, a4) => f(t1(a1), t2(a2), t3(a3), t4(a4)) }
    def apply[T1, T2, T3, T4, T5](f: (T1, T2, T3, T4, T5) => Any)(implicit m1:Manifest[T1], t1:Transformation[T1], m2:Manifest[T2], t2:Transformation[T2], m3:Manifest[T3], t3:Transformation[T3], m4:Manifest[T4], t4:Transformation[T4], m5:Manifest[T5], t5:Transformation[T5]) = doHandle(List(m1, m2, m3, m4, m5)){ case List(a1, a2, a3, a4, a5) => f(t1(a1), t2(a2), t3(a3), t4(a4), t5(a5)) }
    def apply[T1, T2, T3, T4, T5, T6](f: (T1, T2, T3, T4, T5, T6) => Any)(implicit m1:Manifest[T1], t1:Transformation[T1], m2:Manifest[T2], t2:Transformation[T2], m3:Manifest[T3], t3:Transformation[T3], m4:Manifest[T4], t4:Transformation[T4], m5:Manifest[T5], t5:Transformation[T5], m6:Manifest[T6], t6:Transformation[T6]) = doHandle(List(m1, m2, m3, m4, m5, m6)){ case List(a1, a2, a3, a4, a5, a6) => f(t1(a1), t2(a2), t3(a3), t4(a4), t5(a5), t6(a6)) }
    def apply[T1, T2, T3, T4, T5, T6, T7](f: (T1, T2, T3, T4, T5, T6, T7) => Any)(implicit m1:Manifest[T1], t1:Transformation[T1], m2:Manifest[T2], t2:Transformation[T2], m3:Manifest[T3], t3:Transformation[T3], m4:Manifest[T4], t4:Transformation[T4], m5:Manifest[T5], t5:Transformation[T5], m6:Manifest[T6], t6:Transformation[T6], m7:Manifest[T7], t7:Transformation[T7]) = doHandle(List(m1, m2, m3, m4, m5, m6, m7)){ case List(a1, a2, a3, a4, a5, a6, a7) => f(t1(a1), t2(a2), t3(a3), t4(a4), t5(a5), t6(a6), t7(a7)) }
    def apply[T1, T2, T3, T4, T5, T6, T7, T8](f: (T1, T2, T3, T4, T5, T6, T7, T8) => Any)(implicit m1:Manifest[T1], t1:Transformation[T1], m2:Manifest[T2], t2:Transformation[T2], m3:Manifest[T3], t3:Transformation[T3], m4:Manifest[T4], t4:Transformation[T4], m5:Manifest[T5], t5:Transformation[T5], m6:Manifest[T6], t6:Transformation[T6], m7:Manifest[T7], t7:Transformation[T7], m8:Manifest[T8], t8:Transformation[T8]) = doHandle(List(m1, m2, m3, m4, m5, m6, m7, m8)){ case List(a1, a2, a3, a4, a5, a6, a7, a8) => f(t1(a1), t2(a2), t3(a3), t4(a4), t5(a5), t6(a6), t7(a7), t8(a8)) }
    def apply[T1, T2, T3, T4, T5, T6, T7, T8, T9](f: (T1, T2, T3, T4, T5, T6, T7, T8, T9) => Any)(implicit m1:Manifest[T1], t1:Transformation[T1], m2:Manifest[T2], t2:Transformation[T2], m3:Manifest[T3], t3:Transformation[T3], m4:Manifest[T4], t4:Transformation[T4], m5:Manifest[T5], t5:Transformation[T5], m6:Manifest[T6], t6:Transformation[T6], m7:Manifest[T7], t7:Transformation[T7], m8:Manifest[T8], t8:Transformation[T8], m9:Manifest[T9], t9:Transformation[T9]) = doHandle(List(m1, m2, m3, m4, m5, m6, m7, m8, m9)){ case List(a1, a2, a3, a4, a5, a6, a7, a8, a9) => f(t1(a1), t2(a2), t3(a3), t4(a4), t5(a5), t6(a6), t7(a7), t8(a8), t9(a9)) }
    def apply[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10](f: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10) => Any)(implicit m1:Manifest[T1], t1:Transformation[T1], m2:Manifest[T2], t2:Transformation[T2], m3:Manifest[T3], t3:Transformation[T3], m4:Manifest[T4], t4:Transformation[T4], m5:Manifest[T5], t5:Transformation[T5], m6:Manifest[T6], t6:Transformation[T6], m7:Manifest[T7], t7:Transformation[T7], m8:Manifest[T8], t8:Transformation[T8], m9:Manifest[T9], t9:Transformation[T9], m10:Manifest[T10], t10:Transformation[T10]) = doHandle(List(m1, m2, m3, m4, m5, m6, m7, m8, m9, m10)){ case List(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10) => f(t1(a1), t2(a2), t3(a3), t4(a4), t5(a5), t6(a6), t7(a7), t8(a8), t9(a9), t10(a10)) }
    def apply[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11](f: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11) => Any)(implicit m1:Manifest[T1], t1:Transformation[T1], m2:Manifest[T2], t2:Transformation[T2], m3:Manifest[T3], t3:Transformation[T3], m4:Manifest[T4], t4:Transformation[T4], m5:Manifest[T5], t5:Transformation[T5], m6:Manifest[T6], t6:Transformation[T6], m7:Manifest[T7], t7:Transformation[T7], m8:Manifest[T8], t8:Transformation[T8], m9:Manifest[T9], t9:Transformation[T9], m10:Manifest[T10], t10:Transformation[T10], m11:Manifest[T11], t11:Transformation[T11]) = doHandle(List(m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, m11)){ case List(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11) => f(t1(a1), t2(a2), t3(a3), t4(a4), t5(a5), t6(a6), t7(a7), t8(a8), t9(a9), t10(a10), t11(a11)) }
    def apply[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12](f: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12) => Any)(implicit m1:Manifest[T1], t1:Transformation[T1], m2:Manifest[T2], t2:Transformation[T2], m3:Manifest[T3], t3:Transformation[T3], m4:Manifest[T4], t4:Transformation[T4], m5:Manifest[T5], t5:Transformation[T5], m6:Manifest[T6], t6:Transformation[T6], m7:Manifest[T7], t7:Transformation[T7], m8:Manifest[T8], t8:Transformation[T8], m9:Manifest[T9], t9:Transformation[T9], m10:Manifest[T10], t10:Transformation[T10], m11:Manifest[T11], t11:Transformation[T11], m12:Manifest[T12], t12:Transformation[T12]) = doHandle(List(m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, m11, m12)){ case List(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12) => f(t1(a1), t2(a2), t3(a3), t4(a4), t5(a5), t6(a6), t7(a7), t8(a8), t9(a9), t10(a10), t11(a11), t12(a12)) }
    def apply[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13](f: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13) => Any)(implicit m1:Manifest[T1], t1:Transformation[T1], m2:Manifest[T2], t2:Transformation[T2], m3:Manifest[T3], t3:Transformation[T3], m4:Manifest[T4], t4:Transformation[T4], m5:Manifest[T5], t5:Transformation[T5], m6:Manifest[T6], t6:Transformation[T6], m7:Manifest[T7], t7:Transformation[T7], m8:Manifest[T8], t8:Transformation[T8], m9:Manifest[T9], t9:Transformation[T9], m10:Manifest[T10], t10:Transformation[T10], m11:Manifest[T11], t11:Transformation[T11], m12:Manifest[T12], t12:Transformation[T12], m13:Manifest[T13], t13:Transformation[T13]) = doHandle(List(m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, m11, m12, m13)){ case List(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13) => f(t1(a1), t2(a2), t3(a3), t4(a4), t5(a5), t6(a6), t7(a7), t8(a8), t9(a9), t10(a10), t11(a11), t12(a12), t13(a13)) }
    def apply[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14](f: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14) => Any)(implicit m1:Manifest[T1], t1:Transformation[T1], m2:Manifest[T2], t2:Transformation[T2], m3:Manifest[T3], t3:Transformation[T3], m4:Manifest[T4], t4:Transformation[T4], m5:Manifest[T5], t5:Transformation[T5], m6:Manifest[T6], t6:Transformation[T6], m7:Manifest[T7], t7:Transformation[T7], m8:Manifest[T8], t8:Transformation[T8], m9:Manifest[T9], t9:Transformation[T9], m10:Manifest[T10], t10:Transformation[T10], m11:Manifest[T11], t11:Transformation[T11], m12:Manifest[T12], t12:Transformation[T12], m13:Manifest[T13], t13:Transformation[T13], m14:Manifest[T14], t14:Transformation[T14]) = doHandle(List(m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, m11, m12, m13, m14)){ case List(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13, a14) => f(t1(a1), t2(a2), t3(a3), t4(a4), t5(a5), t6(a6), t7(a7), t8(a8), t9(a9), t10(a10), t11(a11), t12(a12), t13(a13), t14(a14)) }
    def apply[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15](f: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15) => Any)(implicit m1:Manifest[T1], t1:Transformation[T1], m2:Manifest[T2], t2:Transformation[T2], m3:Manifest[T3], t3:Transformation[T3], m4:Manifest[T4], t4:Transformation[T4], m5:Manifest[T5], t5:Transformation[T5], m6:Manifest[T6], t6:Transformation[T6], m7:Manifest[T7], t7:Transformation[T7], m8:Manifest[T8], t8:Transformation[T8], m9:Manifest[T9], t9:Transformation[T9], m10:Manifest[T10], t10:Transformation[T10], m11:Manifest[T11], t11:Transformation[T11], m12:Manifest[T12], t12:Transformation[T12], m13:Manifest[T13], t13:Transformation[T13], m14:Manifest[T14], t14:Transformation[T14], m15:Manifest[T15], t15:Transformation[T15]) = doHandle(List(m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, m11, m12, m13, m14, m15)){ case List(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13, a14, a15) => f(t1(a1), t2(a2), t3(a3), t4(a4), t5(a5), t6(a6), t7(a7), t8(a8), t9(a9), t10(a10), t11(a11), t12(a12), t13(a13), t14(a14), t15(a15)) }
    def apply[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16](f: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16) => Any)(implicit m1:Manifest[T1], t1:Transformation[T1], m2:Manifest[T2], t2:Transformation[T2], m3:Manifest[T3], t3:Transformation[T3], m4:Manifest[T4], t4:Transformation[T4], m5:Manifest[T5], t5:Transformation[T5], m6:Manifest[T6], t6:Transformation[T6], m7:Manifest[T7], t7:Transformation[T7], m8:Manifest[T8], t8:Transformation[T8], m9:Manifest[T9], t9:Transformation[T9], m10:Manifest[T10], t10:Transformation[T10], m11:Manifest[T11], t11:Transformation[T11], m12:Manifest[T12], t12:Transformation[T12], m13:Manifest[T13], t13:Transformation[T13], m14:Manifest[T14], t14:Transformation[T14], m15:Manifest[T15], t15:Transformation[T15], m16:Manifest[T16], t16:Transformation[T16]) = doHandle(List(m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, m11, m12, m13, m14, m15, m16)){ case List(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13, a14, a15, a16) => f(t1(a1), t2(a2), t3(a3), t4(a4), t5(a5), t6(a6), t7(a7), t8(a8), t9(a9), t10(a10), t11(a11), t12(a12), t13(a13), t14(a14), t15(a15), t16(a16)) }
    def apply[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17](f: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17) => Any)(implicit m1:Manifest[T1], t1:Transformation[T1], m2:Manifest[T2], t2:Transformation[T2], m3:Manifest[T3], t3:Transformation[T3], m4:Manifest[T4], t4:Transformation[T4], m5:Manifest[T5], t5:Transformation[T5], m6:Manifest[T6], t6:Transformation[T6], m7:Manifest[T7], t7:Transformation[T7], m8:Manifest[T8], t8:Transformation[T8], m9:Manifest[T9], t9:Transformation[T9], m10:Manifest[T10], t10:Transformation[T10], m11:Manifest[T11], t11:Transformation[T11], m12:Manifest[T12], t12:Transformation[T12], m13:Manifest[T13], t13:Transformation[T13], m14:Manifest[T14], t14:Transformation[T14], m15:Manifest[T15], t15:Transformation[T15], m16:Manifest[T16], t16:Transformation[T16], m17:Manifest[T17], t17:Transformation[T17]) = doHandle(List(m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, m11, m12, m13, m14, m15, m16, m17)){ case List(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13, a14, a15, a16, a17) => f(t1(a1), t2(a2), t3(a3), t4(a4), t5(a5), t6(a6), t7(a7), t8(a8), t9(a9), t10(a10), t11(a11), t12(a12), t13(a13), t14(a14), t15(a15), t16(a16), t17(a17)) }
    def apply[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18](f: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18) => Any)(implicit m1:Manifest[T1], t1:Transformation[T1], m2:Manifest[T2], t2:Transformation[T2], m3:Manifest[T3], t3:Transformation[T3], m4:Manifest[T4], t4:Transformation[T4], m5:Manifest[T5], t5:Transformation[T5], m6:Manifest[T6], t6:Transformation[T6], m7:Manifest[T7], t7:Transformation[T7], m8:Manifest[T8], t8:Transformation[T8], m9:Manifest[T9], t9:Transformation[T9], m10:Manifest[T10], t10:Transformation[T10], m11:Manifest[T11], t11:Transformation[T11], m12:Manifest[T12], t12:Transformation[T12], m13:Manifest[T13], t13:Transformation[T13], m14:Manifest[T14], t14:Transformation[T14], m15:Manifest[T15], t15:Transformation[T15], m16:Manifest[T16], t16:Transformation[T16], m17:Manifest[T17], t17:Transformation[T17], m18:Manifest[T18], t18:Transformation[T18]) = doHandle(List(m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, m11, m12, m13, m14, m15, m16, m17, m18)){ case List(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13, a14, a15, a16, a17, a18) => f(t1(a1), t2(a2), t3(a3), t4(a4), t5(a5), t6(a6), t7(a7), t8(a8), t9(a9), t10(a10), t11(a11), t12(a12), t13(a13), t14(a14), t15(a15), t16(a16), t17(a17), t18(a18)) }
    def apply[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19](f: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19) => Any)(implicit m1:Manifest[T1], t1:Transformation[T1], m2:Manifest[T2], t2:Transformation[T2], m3:Manifest[T3], t3:Transformation[T3], m4:Manifest[T4], t4:Transformation[T4], m5:Manifest[T5], t5:Transformation[T5], m6:Manifest[T6], t6:Transformation[T6], m7:Manifest[T7], t7:Transformation[T7], m8:Manifest[T8], t8:Transformation[T8], m9:Manifest[T9], t9:Transformation[T9], m10:Manifest[T10], t10:Transformation[T10], m11:Manifest[T11], t11:Transformation[T11], m12:Manifest[T12], t12:Transformation[T12], m13:Manifest[T13], t13:Transformation[T13], m14:Manifest[T14], t14:Transformation[T14], m15:Manifest[T15], t15:Transformation[T15], m16:Manifest[T16], t16:Transformation[T16], m17:Manifest[T17], t17:Transformation[T17], m18:Manifest[T18], t18:Transformation[T18], m19:Manifest[T19], t19:Transformation[T19]) = doHandle(List(m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, m11, m12, m13, m14, m15, m16, m17, m18, m19)){ case List(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13, a14, a15, a16, a17, a18, a19) => f(t1(a1), t2(a2), t3(a3), t4(a4), t5(a5), t6(a6), t7(a7), t8(a8), t9(a9), t10(a10), t11(a11), t12(a12), t13(a13), t14(a14), t15(a15), t16(a16), t17(a17), t18(a18), t19(a19)) }
    def apply[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20](f: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20) => Any)(implicit m1:Manifest[T1], t1:Transformation[T1], m2:Manifest[T2], t2:Transformation[T2], m3:Manifest[T3], t3:Transformation[T3], m4:Manifest[T4], t4:Transformation[T4], m5:Manifest[T5], t5:Transformation[T5], m6:Manifest[T6], t6:Transformation[T6], m7:Manifest[T7], t7:Transformation[T7], m8:Manifest[T8], t8:Transformation[T8], m9:Manifest[T9], t9:Transformation[T9], m10:Manifest[T10], t10:Transformation[T10], m11:Manifest[T11], t11:Transformation[T11], m12:Manifest[T12], t12:Transformation[T12], m13:Manifest[T13], t13:Transformation[T13], m14:Manifest[T14], t14:Transformation[T14], m15:Manifest[T15], t15:Transformation[T15], m16:Manifest[T16], t16:Transformation[T16], m17:Manifest[T17], t17:Transformation[T17], m18:Manifest[T18], t18:Transformation[T18], m19:Manifest[T19], t19:Transformation[T19], m20:Manifest[T20], t20:Transformation[T20]) = doHandle(List(m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, m11, m12, m13, m14, m15, m16, m17, m18, m19, m20)){ case List(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13, a14, a15, a16, a17, a18, a19, a20) => f(t1(a1), t2(a2), t3(a3), t4(a4), t5(a5), t6(a6), t7(a7), t8(a8), t9(a9), t10(a10), t11(a11), t12(a12), t13(a13), t14(a14), t15(a15), t16(a16), t17(a17), t18(a18), t19(a19), t20(a20)) }
    def apply[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21](f: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21) => Any)(implicit m1:Manifest[T1], t1:Transformation[T1], m2:Manifest[T2], t2:Transformation[T2], m3:Manifest[T3], t3:Transformation[T3], m4:Manifest[T4], t4:Transformation[T4], m5:Manifest[T5], t5:Transformation[T5], m6:Manifest[T6], t6:Transformation[T6], m7:Manifest[T7], t7:Transformation[T7], m8:Manifest[T8], t8:Transformation[T8], m9:Manifest[T9], t9:Transformation[T9], m10:Manifest[T10], t10:Transformation[T10], m11:Manifest[T11], t11:Transformation[T11], m12:Manifest[T12], t12:Transformation[T12], m13:Manifest[T13], t13:Transformation[T13], m14:Manifest[T14], t14:Transformation[T14], m15:Manifest[T15], t15:Transformation[T15], m16:Manifest[T16], t16:Transformation[T16], m17:Manifest[T17], t17:Transformation[T17], m18:Manifest[T18], t18:Transformation[T18], m19:Manifest[T19], t19:Transformation[T19], m20:Manifest[T20], t20:Transformation[T20], m21:Manifest[T21], t21:Transformation[T21]) = doHandle(List(m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, m11, m12, m13, m14, m15, m16, m17, m18, m19, m20, m21)){ case List(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13, a14, a15, a16, a17, a18, a19, a20, a21) => f(t1(a1), t2(a2), t3(a3), t4(a4), t5(a5), t6(a6), t7(a7), t8(a8), t9(a9), t10(a10), t11(a11), t12(a12), t13(a13), t14(a14), t15(a15), t16(a16), t17(a17), t18(a18), t19(a19), t20(a20), t21(a21)) }
    def apply[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22](f: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22) => Any)(implicit m1:Manifest[T1], t1:Transformation[T1], m2:Manifest[T2], t2:Transformation[T2], m3:Manifest[T3], t3:Transformation[T3], m4:Manifest[T4], t4:Transformation[T4], m5:Manifest[T5], t5:Transformation[T5], m6:Manifest[T6], t6:Transformation[T6], m7:Manifest[T7], t7:Transformation[T7], m8:Manifest[T8], t8:Transformation[T8], m9:Manifest[T9], t9:Transformation[T9], m10:Manifest[T10], t10:Transformation[T10], m11:Manifest[T11], t11:Transformation[T11], m12:Manifest[T12], t12:Transformation[T12], m13:Manifest[T13], t13:Transformation[T13], m14:Manifest[T14], t14:Transformation[T14], m15:Manifest[T15], t15:Transformation[T15], m16:Manifest[T16], t16:Transformation[T16], m17:Manifest[T17], t17:Transformation[T17], m18:Manifest[T18], t18:Transformation[T18], m19:Manifest[T19], t19:Transformation[T19], m20:Manifest[T20], t20:Transformation[T20], m21:Manifest[T21], t21:Transformation[T21], m22:Manifest[T22], t22:Transformation[T22]) = doHandle(List(m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, m11, m12, m13, m14, m15, m16, m17, m18, m19, m20, m21, m22)){ case List(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13, a14, a15, a16, a17, a18, a19, a20, a21, a22) => f(t1(a1), t2(a2), t3(a3), t4(a4), t5(a5), t6(a6), t7(a7), t8(a8), t9(a9), t10(a10), t11(a11), t12(a12), t13(a13), t14(a14), t15(a15), t16(a16), t17(a17), t18(a18), t19(a19), t20(a20), t21(a21), t22(a22)) }

    protected def handle(f:List[Any] => Any, signature:String)

    private def doHandle(manifests:List[Manifest[_]])(pf:PartialFunction[List[Any], Any]) = {
      def s(list:List[_]) = if(list.length != 1) "s" else ""
      val sig = signature(manifests)
      val f:List[Any] => Any = pf orElse {
        case x =>
          throw JRuby.cucumberArityMismatchError("Your block takes "+manifests.length+" argument" + s(manifests)+", but the Regexp matched "+x.length+" argument"+s(x)) 
      }
      handle(f, sig)
    }

    private def signature(manifests:List[Manifest[_]]) = {
      manifests.map(_.erasure.getSimpleName) match {
        case one :: Nil => one
        case zero_or_many => zero_or_many.mkString("(", ",", ")")
      }
    }
  }

  //PRIVATE

  private def illegalState(msg:String) = throw new IllegalStateException(msg)

  private [ScalaDsl] trait Mode {
    def createHandle(name: String, regex: String): StepBody

    def callTable(name: String, regex: String, table: Table): Unit

    def callPy(name: String, regex: String, py: String): Unit

    def ask(question:String, timout:Int):String

    def announce(message:String):Unit

    def embed(file:String, mimeType:String):Unit
  }

  /*
  default 'recording-mode' instance
   */
  private var mode: Mode = new Mode {
    override def createHandle(name: String, regex: String) = new StepBody {
      def handle(f:List[Any] => Any, signature:String) = stepDefinitions += (new ScalaStepDefinition(name, regex, f, signature, _:AbstractProgrammingLanguage))
    }

    override def callTable(name: String, regex: String, table: Table) = illegalState(name + "(" + regex + ", 'Table') is only intended for calling other steps")

    override def callPy(name: String, regex: String, py: String) = illegalState(name + "(" + regex + ", 'String') is only intended for calling other steps")

    override def ask(question:String, timeout:Int) = illegalState("'ask' is only intended to be used from inside a Step")

    override def announce(message:String) = illegalState("'announce' is only intended to used from inside a Step")

    override def embed(file:String, mimeType:String) = illegalState("'embed' is only intended to be used from inside a Step")
  }

  /*
  gets called by 'ScalaAnalyzer' after recording of step definitions is done.
  switches to 'execution-mode'
   */
  private[cuke4duke] def executionMode(stepMother: StepMother) {
    mode = new Mode {
      override def createHandle(name: String, regex: String) = new StepBody {
        stepMother.invoke(regex)
        def handle(f:List[Any] => Any, s:String) = illegalState("cannot register new stepdefinitions in execution mode")
      }

      override def callTable(name: String, regex: String, table: Table) = stepMother.invoke(regex, table)

      override def callPy(name: String, regex: String, py: String) = stepMother.invoke(regex, py)

      override def ask(question:String, timeout:Int) = stepMother.ask(question, timeout)

      override def announce(message:String) = stepMother.announce(message)

      override def embed(file:String, mimeType:String) = stepMother.embed(file, mimeType)
    }
  }
}

/* transformation from Any -> T */
trait Transformation[T] {
  def apply(a: Any): T
}

/* specialised version transforming 'String -> T' (treating pyStrings as Strings)*/
trait Transform[T] extends Transformation[T] {
  override final def apply(a: Any): T = a match {
    case py: PyString => transform(py.to_s)
    case s: String => transform(s)
    case unknown => error("Dont know how to transform " + unknown)
  }

  def transform(value: String): T
}

/* specialised version transforming 'Table -> T' */
trait TransformTable[T] extends Transformation[T] {
  override final def apply(a: Any): T = a match {
    case table: Table => transform(table)
    case unknown => error("Dont know how to transform " + unknown)
  }

  def transform(value: Table): T
}

/* object for easing the creation of string transformations */
object Transform {
  def apply[T](f: String => T): Transformation[T] = new Transform[T] {
    def transform(value: String) = f(value)
  }
}

/* object for easing the creation of table transformations */
object TransformTable {
  def apply[T](f: Table => T): Transformation[T] = new TransformTable[T] {
    def transform(table: Table) = f(table)
  }
}

/* Default transformations */
object Transformation {
  implicit val t2table: Transformation[Table] = TransformTable(x => x)

  implicit val t2Int: Transformation[Int] = Transform(_.toInt)
  implicit val t2Long: Transformation[Long] = Transform(_.toLong)
  implicit val t2String: Transformation[String] = Transform(x => x)
  implicit val t2Double: Transformation[Double] = Transform(_.toDouble)
  implicit val t2Float: Transformation[Float] = Transform(_.toFloat)
  implicit val t2Short: Transformation[Short] = Transform(_.toShort)
  implicit val t2Byte: Transformation[Byte] = Transform(_.toByte)
  implicit val t2BigDecimal: Transformation[BigDecimal] = Transform(BigDecimal(_))
  implicit val t2BigInt: Transformation[BigInt] = Transform(BigInt(_))
  implicit val t2Char: Transformation[Char] = Transform(_.charAt(0))
  implicit val t2Boolean: Transformation[Boolean] = Transform(_.toBoolean)

}

