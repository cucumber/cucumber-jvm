package cuke4duke.internal.scala

import collection.immutable.TreeMap
import cuke4duke.internal.language.{JdkPatternArgumentMatcher, StepDefinition}
import _root_.java.util.regex.Pattern
import cuke4duke.internal.JRuby
import org.jruby.RubyArray

class ScalaStepDefinition(name:String, r: String, f: Any, types: List[Class[_]], conversions:TreeMap[Class[_], String => Option[_]]) extends StepDefinition {

  private val pattern = Pattern.compile(r)

  def file_colon_line = name + "(\""+r+"\"){ "+argTypes+" => ... }"

  private val argTypes = types.map(_.getSimpleName) match {
    case one :: Nil => one
    case zero_or_many => zero_or_many.mkString("(",",",")")
  }

  def regexp_source = r

  def arguments_from(step_name:String) = JdkPatternArgumentMatcher.argumentsFrom(pattern, step_name)

  def invoke(ra: RubyArray) {
    transform(ra.toArray.toList.map(_.toString), types) match {
      case List() => f.asInstanceOf[Function0[_]]()
      case List(t1) => f.asInstanceOf[Function1[Any, _]](t1)
      case List(t1, t2) => f.asInstanceOf[Function2[Any, Any, _]](t1, t2)
      case List(t1, t2, t3) => f.asInstanceOf[Function3[Any, Any, Any, _]](t1, t2, t3)
      case List(t1, t2, t3, t4) => f.asInstanceOf[Function4[Any, Any, Any, Any, _]](t1, t2, t3, t4)
      case List(t1, t2, t3, t4, t5) => f.asInstanceOf[Function5[Any, Any, Any, Any, Any, _]](t1, t2, t3, t4, t5)
      case List(t1, t2, t3, t4, t5, t6) => f.asInstanceOf[Function6[Any, Any, Any, Any, Any, Any, _]](t1, t2, t3, t4, t5, t6)
      case List(t1, t2, t3, t4, t5, t6, t7) => f.asInstanceOf[Function7[Any, Any, Any, Any, Any, Any, Any, _]](t1, t2, t3, t4, t5, t6, t7)
      case List(t1, t2, t3, t4, t5, t6, t7, t8) => f.asInstanceOf[Function8[Any, Any, Any, Any, Any, Any, Any, Any, _]](t1, t2, t3, t4, t5, t6, t7, t8)
      case List(t1, t2, t3, t4, t5, t6, t7, t8, t9) => f.asInstanceOf[Function9[Any, Any, Any, Any, Any, Any, Any, Any, Any, _]](t1, t2, t3, t4, t5, t6, t7, t8, t9)
      case List(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10) => f.asInstanceOf[Function10[Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, _]](t1, t2, t3, t4, t5, t6, t7, t8, t9, t10)
      case List(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11) => f.asInstanceOf[Function11[Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, _]](t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11)
      case List(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12) => f.asInstanceOf[Function12[Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, _]](t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12)
      case List(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13) => f.asInstanceOf[Function13[Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, _]](t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13)
      case List(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14) => f.asInstanceOf[Function14[Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, _]](t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14)
      case List(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15) => f.asInstanceOf[Function15[Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, _]](t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15)
      case List(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16) => f.asInstanceOf[Function16[Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, _]](t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16)
      case List(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16, t17) => f.asInstanceOf[Function17[Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, _]](t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16, t17)
      case List(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16, t17, t18) => f.asInstanceOf[Function18[Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, _]](t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16, t17, t18)
      case List(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16, t17, t18, t19) => f.asInstanceOf[Function19[Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, _]](t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16, t17, t18, t19)
      case List(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16, t17, t18, t19, t20) => f.asInstanceOf[Function20[Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, _]](t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16, t17, t18, t19, t20)
      case List(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16, t17, t18, t19, t20, t21) => f.asInstanceOf[Function21[Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, _]](t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16, t17, t18, t19, t20, t21)
      case List(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16, t17, t18, t19, t20, t21, t22) => f.asInstanceOf[Function22[Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, _]](t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16, t17, t18, t19, t20, t21, t22)
    }
  }

  private [this] def transform(args:List[String], types:List[Class[_]]) = {
    if(args.length != types.length){
      def s(list:List[_]) = if(list.length != 1) "s" else ""
      throw JRuby.cucumberArityMismatchError("Your block takes "+types.length+" argument" + s(types)+", but the Regexp matched "+args.length+" argument"+s(args))
    } else {
      for((value, kind) <- args zip types)
        yield {
          convert(value, kind).getOrElse(throw JRuby.cucumberUndefined("No conversion defined from value '"+value+"' to "+kind))
        }
    }
  }

  private def convert(value:String, to:Class[_]):Option[_] = {
      val start:Option[_] = if(to.isAssignableFrom(value.getClass)) Some(value) else None

      (start /: conversions.elements){ (acc, entry) =>
        acc match {
          case None if to.isAssignableFrom(entry._1) => entry._2(value)
          case _ => acc
        }
      }
    }

}