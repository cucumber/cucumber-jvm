package cuke4duke.internal.scala

import collection.immutable.TreeMap
import reflect.Manifest
import cuke4duke.internal.JRuby

private [cuke4duke] class ScalaTransformations {

  implicit def orderedClass(a:Class[_]) = new Ordered[Class[_]]{
    def compare(that: Class[_]) = {
      if(a == that) 0
      else if(that.isAssignableFrom(a)) 1
      else -1
    }
  }

  private var transformations = new TreeMap[Class[_], String => Option[_]]

  def attempt[_](transformation:String => Option[_]) =
      (s:String) => {
        try{
          transformation(s)
        } catch {
          case _ => None
        }
      }

  def addAll(t:Iterable[(Class[_], String => Option[_])]){
    for((key, value) <- t){
      transformations = transformations.update(key, attempt(value))
    }
  }

  def Transform[T](f:String => Option[T])(implicit m:Manifest[T]){
    addAll((m.erasure, attempt(f)) :: Nil)
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

  def transform(args:List[String], types:List[Class[_]]) = {
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

  def convert(value:String, to:Class[_]):Option[_] = {
      val start:Option[_] = if(to.isAssignableFrom(value.getClass)) Some(value) else None

      (start /: transformations.elements){ (acc, entry) =>
        acc match {
          case None if to.isAssignableFrom(entry._1) => entry._2(value)
          case _ => acc
        }
      }
    }
}