package cucumber.runtime.scala

import _root_.cucumber.api.DataTable

trait Transform[T] {
  def apply(a: Any): T
}

object Transform {

  def apply[A,B](f:A => B):Transform[B] = new Transform[B]{
    def apply(a:Any) = a match {
      case s:A => f(s)
    }
  }

  implicit val t2table: Transform[DataTable] = new Transform[DataTable]{
    def apply(a:Any) = a match {
      case t:DataTable => t
    }
  }

  implicit val t2Int = Transform((i:java.lang.Integer) => i.toInt)
  implicit val t2Long = Transform((l:java.lang.Long) => l.toLong)
  implicit val t2String = Transform((s:java.lang.String) => s)
  implicit val t2Double = Transform((d:java.lang.Double) => d.toDouble)
  implicit val t2Float = Transform((f:java.lang.Float) => f.toFloat)
  implicit val t2Short = Transform((s:java.lang.Short) => s.toShort)
  implicit val t2Byte = Transform((b:java.lang.Byte) => b.toByte)
  implicit val t2BigDecimal = Transform((bd:java.math.BigDecimal) => BigDecimal(bd))
  implicit val t2BigInt = Transform((bi:java.math.BigInteger) => new BigInt(bi))
  implicit val t2Char = Transform((c:java.lang.Character) => c:Char)
  implicit val t2Boolean = Transform((b:java.lang.Boolean) => b:Boolean)
}
