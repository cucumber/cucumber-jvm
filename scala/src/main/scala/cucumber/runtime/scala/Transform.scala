package cucumber.runtime.scala

import _root_.cucumber.api.DataTable

trait Transform[T] {
  def apply(a: Any): T
}

object Transform {

  def apply[A](f:String => A):Transform[A] = new Transform[A]{
    def apply(a:Any) = a match {
      case s:String => f(s)
    }
  }

  implicit val t2table: Transform[DataTable] = new Transform[DataTable]{
    def apply(a:Any) = a match {
      case t:DataTable => t
    }
  }

  implicit val t2Int        = Transform(_.toInt)
  implicit val t2Long       = Transform(_.toLong)
  implicit val t2String     = Transform(identity)
  implicit val t2Double     = Transform(_.toDouble)
  implicit val t2Float      = Transform(_.toFloat)
  implicit val t2Short      = Transform(_.toShort)
  implicit val t2Byte       = Transform(_.toByte)
  implicit val t2BigDecimal = Transform(BigDecimal(_))
  implicit val t2BigInt     = Transform(BigInt(_))
  implicit val t2Char       = Transform(_.charAt(0))
  implicit val t2Boolean    = Transform(_.toBoolean)
}
