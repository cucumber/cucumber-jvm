package cucumber.examples.scalacalculator

import collection.mutable.Stack

sealed trait Arg

object Arg{
  implicit def op(s:String) = Op(s)
  implicit def value(v:Double) = Val(v)
}

case class Op(value: String) extends Arg
case class Val(value: Double) extends Arg

class RpnCalculator {
  private val stack = new Stack[Double]

  private def op(f: (Double, Double) => Double) =
    stack push f(stack.pop(), stack.pop())

  def push(arg: Arg) {
    arg match {
      case Op("+") => op(_ + _)
      case Op("-") => op(_ - _)
      case Op("*") => op(_ * _)
      case Op("/") => op(_ / _)
      case Val(value) => stack push value
    }
  }

  def value = stack.head
}