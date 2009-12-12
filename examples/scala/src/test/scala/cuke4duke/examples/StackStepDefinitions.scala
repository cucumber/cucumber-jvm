package cuke4duke.examples

import cuke4duke.ScalaDsl
import collection.mutable.Stack
import org.junit.Assert._

class StackStepDefinitions extends ScalaDsl {

  val stack:Stack[Int] = new Stack[Int]

  Given("I have an empty stack") {
    stack.clear()
  }

  When("""I pøsh (.) onto the stack""") { something:Char =>

    def calc(f:(Int, Int) => Int) {
      while(stack.size > 1) {
        val a = stack.pop
        val b = stack.pop
        stack.push(f(b, a))
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
    assertEquals(i, stack.top)
  }

  Then("""the size should be (\d)""") { i:Int =>
    assertEquals(i, stack.size)
  }

  Then("""this should never be run, and it should show up in target/usage.txt""") {
    assertEquals(1, 2)
  }
}
