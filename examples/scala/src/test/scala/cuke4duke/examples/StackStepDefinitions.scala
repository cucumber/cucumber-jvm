package cuke4duke.examples

import cuke4duke.ScalaDsl
import collection.mutable.Stack

class StackStepDefinitions extends ScalaDsl {

  val stack:Stack[Int] = new Stack[Int]

  Given("I have an empty stack") {
    stack.clear()
  }

  When("""I push (.) onto the stack""") { something:Char =>

    def calc(f:(Int, Int) => Int){
      while(stack.size > 1){
        stack.push(f(stack.pop, stack.pop))
      }
    }

    something match {
      case i if i.isDigit => stack.push(something.toString.toInt)
      case '+' => calc(_ + _)
      case '-' => calc(_ - _)
      case '*' => calc(_ * _)
      case '/' => calc(_ / _)
    }
  
  }

  Then("""the top should be (\d)""") { i:Int =>
    assert(stack.top == i)
  }

  Then("""the size should be (\d)""") { i:Int =>
    assert(stack.size == i)
  }
}
