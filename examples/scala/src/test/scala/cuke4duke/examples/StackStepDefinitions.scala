package cuke4duke.examples

import cuke4duke.ScalaDsl
import collection.immutable.Stack

class StackStepDefinitions extends ScalaDsl {

  var stack:Stack[Int] = _

  Given("I have an empty stack") {
    stack = new Stack[Int]
  }

  When("""I push (.) onto the stack""") { something:Char =>
    stack = stack.push(something match {
      case i if i.isDigit => something.toString.toInt
      case '+' => stack.reduceLeft{ _ + _ }
      case '-' => stack.reduceLeft{ _ - _ }
      case '*' => stack.reduceLeft{ _ * _ }
      case '/' => stack.reduceLeft{ _ / _ }
    })
  }

  Then("""the top should be (\d)""") { i:Int =>
    assert(stack.top == i)
  }
}
