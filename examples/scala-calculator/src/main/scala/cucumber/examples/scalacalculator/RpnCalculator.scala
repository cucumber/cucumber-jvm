package cucumber.examples.scalacalculator

import scala.collection.mutable.Queue


sealed trait Arg

object Arg{
  implicit def op(s:String) = Op(s)
  implicit def value(v:Double) = Val(v)
}

case class Op(value: String) extends Arg
case class Val(value: Double) extends Arg

class RpnCalculator {
  private val stack = new Queue[Double]

  private def op(f: (Double, Double) => Double) =
    stack += f(stack.dequeue(), stack.dequeue())

  def push(arg: Arg) {
    arg match {
      case Op("+") => op(_ + _)
      case Op("-") => op(_ - _)
      case Op("*") => op(_ * _)
      case Op("/") => op(_ / _)
      case Val(value) => stack += value
    }
  }

  def value = stack.head
}