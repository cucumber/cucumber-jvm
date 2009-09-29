package cuke4duke

import _root_.scala.collection.mutable.ListBuffer
import _root_.scala.reflect.Manifest
import internal.JRuby
import internal.language.ProgrammingLanguage
import internal.scala.{ScalaHook, ScalaStepDefinition}
import collection.immutable.TreeMap

/*
  <yourclass> {extends|with} ScalaDsl
 */
trait ScalaDsl {

  private [cuke4duke] val stepDefinitions = new ListBuffer[ProgrammingLanguage => ScalaStepDefinition]
  private [cuke4duke] val beforeHooks = new ListBuffer[ScalaHook]
  private [cuke4duke] val afterHooks = new ListBuffer[ScalaHook]

  val Given = new Step("Given")
  val When = new Step("When")
  val Then = new Step("Then")

  def Before(f: => Unit) = beforeHooks += new ScalaHook(Nil, f _)
  def Before(tags: String*)(f: => Unit) = beforeHooks += new ScalaHook(tags.toList, f _)

  def After(f: => Unit) = afterHooks += new ScalaHook(Nil, f _)
  def After(tags: String*)(f: => Unit) = afterHooks += new ScalaHook(tags.toList, f _)

  def pending(message:String){ throw JRuby.cucumberPending(message) }
  def pending{ pending("TODO") }

  def Transform[T](f:String => Option[T])(implicit m:Manifest[T]){
    transformations = transformations.insert(m.erasure, attempt(f))
  }

  private implicit def orderedClass(a:Class[_]) = new Ordered[Class[_]]{
    def compare(that: Class[_]) = {
      if(a == that) 0
      else if(that.isAssignableFrom(a)) 1
      else -1
    }
  }

  private var transformations = new TreeMap[Class[_], String => Option[_]]

  private def attempt[T](transformation:String => Option[T]) =
      (s:String) => {
        try{
          transformation(s)
        } catch {
          case _ => None
        }
      }

  //default transformations
  Transform[Int](x => Some(x.toInt))
  Transform[Long](x => Some(x.toLong))
  Transform[String](x => Some(x))
  Transform[Double](x => Some(x.toDouble))
  Transform[Float](x => Some(x.toFloat))
  Transform[Short](x => Some(x.toShort))
  Transform[Byte](x => Some(x.toByte))
  Transform[BigDecimal](x => Some(BigDecimal(x)))
  Transform[BigInt](x => Some(BigInt(x)))
  Transform[Char](x => if(x.length == 1) Some(x.charAt(0)) else None)
  Transform[Boolean](x => Some(x.toBoolean))

  final class Step(name:String) {
    def apply(regex:String) = new {
      def apply(f: => Unit):Unit = apply(f0toFun(f _))
      def apply(fun:Fun) = stepDefinitions += ((programmingLanguage:ProgrammingLanguage) => new ScalaStepDefinition(name, regex, fun.f, fun.types, transformations, programmingLanguage))
    }
  }

  final class Fun private[ScalaDsl](private [ScalaDsl] val f: Any, manifests: Manifest[_]*) {
    private [ScalaDsl] val types = manifests.toList.map(_.erasure)
  }

  implicit def f0toFun(f: Function0[_]) = new Fun(f)
  implicit def f1toFun[T1, _](f: Function1[T1, _])(implicit m1: Manifest[T1]) = new Fun(f, m1)
  implicit def f2toFun[T1, T2, _](f: Function2[T1, T2, _])(implicit m1: Manifest[T1], m2: Manifest[T2]) = new Fun(f, m1, m2)
  implicit def f3toFun[T1, T2, T3, _](f: Function3[T1, T2, T3, _])(implicit m1: Manifest[T1], m2: Manifest[T2], m3: Manifest[T3]) = new Fun(f, m1, m2, m3)
  implicit def f4toFun[T1, T2, T3, T4, _](f: Function4[T1, T2, T3, T4, _])(implicit m1: Manifest[T1], m2: Manifest[T2], m3: Manifest[T3], m4: Manifest[T4]) = new Fun(f, m1, m2, m3, m4)
  implicit def f5toFun[T1, T2, T3, T4, T5, _](f: Function5[T1, T2, T3, T4, T5, _])(implicit m1: Manifest[T1], m2: Manifest[T2], m3: Manifest[T3], m4: Manifest[T4], m5: Manifest[T5]) = new Fun(f, m1, m2, m3, m4, m5)
  implicit def f6toFun[T1, T2, T3, T4, T5, T6, _](f: Function6[T1, T2, T3, T4, T5, T6, _])(implicit m1: Manifest[T1], m2: Manifest[T2], m3: Manifest[T3], m4: Manifest[T4], m5: Manifest[T5], m6: Manifest[T6]) = new Fun(f, m1, m2, m3, m4, m5, m6)
  implicit def f7toFun[T1, T2, T3, T4, T5, T6, T7, _](f: Function7[T1, T2, T3, T4, T5, T6, T7, _])(implicit m1: Manifest[T1], m2: Manifest[T2], m3: Manifest[T3], m4: Manifest[T4], m5: Manifest[T5], m6: Manifest[T6], m7: Manifest[T7]) = new Fun(f, m1, m2, m3, m4, m5, m6, m7)
  implicit def f8toFun[T1, T2, T3, T4, T5, T6, T7, T8, _](f: Function8[T1, T2, T3, T4, T5, T6, T7, T8, _])(implicit m1: Manifest[T1], m2: Manifest[T2], m3: Manifest[T3], m4: Manifest[T4], m5: Manifest[T5], m6: Manifest[T6], m7: Manifest[T7], m8: Manifest[T8]) = new Fun(f, m1, m2, m3, m4, m5, m6, m7, m8)
  implicit def f9toFun[T1, T2, T3, T4, T5, T6, T7, T8, T9, _](f: Function9[T1, T2, T3, T4, T5, T6, T7, T8, T9, _])(implicit m1: Manifest[T1], m2: Manifest[T2], m3: Manifest[T3], m4: Manifest[T4], m5: Manifest[T5], m6: Manifest[T6], m7: Manifest[T7], m8: Manifest[T8], m9: Manifest[T9]) = new Fun(f, m1, m2, m3, m4, m5, m6, m7, m8, m9)
  implicit def f10toFun[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, _](f: Function10[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, _])(implicit m1: Manifest[T1], m2: Manifest[T2], m3: Manifest[T3], m4: Manifest[T4], m5: Manifest[T5], m6: Manifest[T6], m7: Manifest[T7], m8: Manifest[T8], m9: Manifest[T9], m10: Manifest[T10]) = new Fun(f, m1, m2, m3, m4, m5, m6, m7, m8, m9, m10)
  implicit def f11toFun[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, _](f: Function11[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, _])(implicit m1: Manifest[T1], m2: Manifest[T2], m3: Manifest[T3], m4: Manifest[T4], m5: Manifest[T5], m6: Manifest[T6], m7: Manifest[T7], m8: Manifest[T8], m9: Manifest[T9], m10: Manifest[T10], m11: Manifest[T11]) = new Fun(f, m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, m11)
  implicit def f12toFun[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, _](f: Function12[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, _])(implicit m1: Manifest[T1], m2: Manifest[T2], m3: Manifest[T3], m4: Manifest[T4], m5: Manifest[T5], m6: Manifest[T6], m7: Manifest[T7], m8: Manifest[T8], m9: Manifest[T9], m10: Manifest[T10], m11: Manifest[T11], m12: Manifest[T12]) = new Fun(f, m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, m11, m12)
  implicit def f13toFun[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, _](f: Function13[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, _])(implicit m1: Manifest[T1], m2: Manifest[T2], m3: Manifest[T3], m4: Manifest[T4], m5: Manifest[T5], m6: Manifest[T6], m7: Manifest[T7], m8: Manifest[T8], m9: Manifest[T9], m10: Manifest[T10], m11: Manifest[T11], m12: Manifest[T12], m13: Manifest[T13]) = new Fun(f, m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, m11, m12, m13)
  implicit def f14toFun[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, _](f: Function14[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, _])(implicit m1: Manifest[T1], m2: Manifest[T2], m3: Manifest[T3], m4: Manifest[T4], m5: Manifest[T5], m6: Manifest[T6], m7: Manifest[T7], m8: Manifest[T8], m9: Manifest[T9], m10: Manifest[T10], m11: Manifest[T11], m12: Manifest[T12], m13: Manifest[T13], m14: Manifest[T14]) = new Fun(f, m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, m11, m12, m13, m14)
  implicit def f15toFun[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, _](f: Function15[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, _])(implicit m1: Manifest[T1], m2: Manifest[T2], m3: Manifest[T3], m4: Manifest[T4], m5: Manifest[T5], m6: Manifest[T6], m7: Manifest[T7], m8: Manifest[T8], m9: Manifest[T9], m10: Manifest[T10], m11: Manifest[T11], m12: Manifest[T12], m13: Manifest[T13], m14: Manifest[T14], m15: Manifest[T15]) = new Fun(f, m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, m11, m12, m13, m14, m15)
  implicit def f16toFun[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, _](f: Function16[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, _])(implicit m1: Manifest[T1], m2: Manifest[T2], m3: Manifest[T3], m4: Manifest[T4], m5: Manifest[T5], m6: Manifest[T6], m7: Manifest[T7], m8: Manifest[T8], m9: Manifest[T9], m10: Manifest[T10], m11: Manifest[T11], m12: Manifest[T12], m13: Manifest[T13], m14: Manifest[T14], m15: Manifest[T15], m16: Manifest[T16]) = new Fun(f, m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, m11, m12, m13, m14, m15, m16)
  implicit def f17toFun[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, _](f: Function17[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, _])(implicit m1: Manifest[T1], m2: Manifest[T2], m3: Manifest[T3], m4: Manifest[T4], m5: Manifest[T5], m6: Manifest[T6], m7: Manifest[T7], m8: Manifest[T8], m9: Manifest[T9], m10: Manifest[T10], m11: Manifest[T11], m12: Manifest[T12], m13: Manifest[T13], m14: Manifest[T14], m15: Manifest[T15], m16: Manifest[T16], m17: Manifest[T17]) = new Fun(f, m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, m11, m12, m13, m14, m15, m16, m17)
  implicit def f18toFun[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, _](f: Function18[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, _])(implicit m1: Manifest[T1], m2: Manifest[T2], m3: Manifest[T3], m4: Manifest[T4], m5: Manifest[T5], m6: Manifest[T6], m7: Manifest[T7], m8: Manifest[T8], m9: Manifest[T9], m10: Manifest[T10], m11: Manifest[T11], m12: Manifest[T12], m13: Manifest[T13], m14: Manifest[T14], m15: Manifest[T15], m16: Manifest[T16], m17: Manifest[T17], m18: Manifest[T18]) = new Fun(f, m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, m11, m12, m13, m14, m15, m16, m17, m18)
  implicit def f19toFun[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, _](f: Function19[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, _])(implicit m1: Manifest[T1], m2: Manifest[T2], m3: Manifest[T3], m4: Manifest[T4], m5: Manifest[T5], m6: Manifest[T6], m7: Manifest[T7], m8: Manifest[T8], m9: Manifest[T9], m10: Manifest[T10], m11: Manifest[T11], m12: Manifest[T12], m13: Manifest[T13], m14: Manifest[T14], m15: Manifest[T15], m16: Manifest[T16], m17: Manifest[T17], m18: Manifest[T18], m19: Manifest[T19]) = new Fun(f, m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, m11, m12, m13, m14, m15, m16, m17, m18, m19)
  implicit def f20toFun[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, _](f: Function20[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, _])(implicit m1: Manifest[T1], m2: Manifest[T2], m3: Manifest[T3], m4: Manifest[T4], m5: Manifest[T5], m6: Manifest[T6], m7: Manifest[T7], m8: Manifest[T8], m9: Manifest[T9], m10: Manifest[T10], m11: Manifest[T11], m12: Manifest[T12], m13: Manifest[T13], m14: Manifest[T14], m15: Manifest[T15], m16: Manifest[T16], m17: Manifest[T17], m18: Manifest[T18], m19: Manifest[T19], m20: Manifest[T20]) = new Fun(f, m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, m11, m12, m13, m14, m15, m16, m17, m18, m19, m20)
  implicit def f21toFun[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, _](f: Function21[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, _])(implicit m1: Manifest[T1], m2: Manifest[T2], m3: Manifest[T3], m4: Manifest[T4], m5: Manifest[T5], m6: Manifest[T6], m7: Manifest[T7], m8: Manifest[T8], m9: Manifest[T9], m10: Manifest[T10], m11: Manifest[T11], m12: Manifest[T12], m13: Manifest[T13], m14: Manifest[T14], m15: Manifest[T15], m16: Manifest[T16], m17: Manifest[T17], m18: Manifest[T18], m19: Manifest[T19], m20: Manifest[T20], m21: Manifest[T21]) = new Fun(f, m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, m11, m12, m13, m14, m15, m16, m17, m18, m19, m20, m21)
  implicit def f22toFun[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22, _](f: Function22[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22, _])(implicit m1: Manifest[T1], m2: Manifest[T2], m3: Manifest[T3], m4: Manifest[T4], m5: Manifest[T5], m6: Manifest[T6], m7: Manifest[T7], m8: Manifest[T8], m9: Manifest[T9], m10: Manifest[T10], m11: Manifest[T11], m12: Manifest[T12], m13: Manifest[T13], m14: Manifest[T14], m15: Manifest[T15], m16: Manifest[T16], m17: Manifest[T17], m18: Manifest[T18], m19: Manifest[T19], m20: Manifest[T20], m21: Manifest[T21], m22: Manifest[T22]) = new Fun(f, m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, m11, m12, m13, m14, m15, m16, m17, m18, m19, m20, m21, m22)
}