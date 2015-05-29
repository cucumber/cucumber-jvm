package cucumber.api.scala

import _root_.org.junit.{Test, Assert}
import Assert._
import _root_.gherkin.I18n
import _root_.gherkin.formatter.model.Tag
import collection.JavaConverters._
import cucumber.api.Scenario

class ScalaDslTest {

  object StubScenario extends Scenario{
    def getSourceTagNames = null

    def getStatus =  ""

    def isFailed = false

    def embed(p1: Array[Byte], p2: String) {}

    def write(p1: String) {}

    def getName = ""

    def getId = ""
  }

  @Test
  def emptyBefore {

    var actualScenario : Scenario = null

    object Befores extends ScalaDsl with EN {
      Before { actualScenario = _ }
    }

    assertEquals(1, Befores.beforeHooks.size)
    val hook = Befores.beforeHooks.head
    assertTrue(hook.matches(List[Tag]().asJava))
    hook.execute(StubScenario)
    assertEquals(Int.MaxValue, hook.getOrder)
    assertEquals(StubScenario, actualScenario)
  }

  @Test
  def taggedBefore {
    var actualScenario : Scenario = null

    object Befores extends ScalaDsl with EN {
      Before("@foo,@bar", "@zap"){ actualScenario = _ }
    }

    assertEquals(1, Befores.beforeHooks.size)

    val hook = Befores.beforeHooks.head
    assertFalse(hook.matches(List[Tag]().asJava))
    assertTrue(hook.matches(List(new Tag("@bar", 0), new Tag("@zap", 0)).asJava))
    assertFalse(hook.matches(List(new Tag("@bar", 1)).asJava))

    hook.execute(StubScenario)
    assertEquals(StubScenario, actualScenario)
    assertEquals(Int.MaxValue, hook.getOrder)
  }

  @Test
  def orderedBefore {

    object Befores extends ScalaDsl with EN {
      Before(10){ scenario : Scenario =>   }
    }

    val hook = Befores.beforeHooks(0)
    assertEquals(10, hook.getOrder)
  }

  @Test
  def taggedOrderedBefore {

    object Befores extends ScalaDsl with EN {
      Before(10, "@foo,@bar", "@zap"){  scenario : Scenario => }
    }

    val hook = Befores.beforeHooks(0)
    assertEquals(10, hook.getOrder)
  }

  @Test
  def emptyAfter {

    var actualScenario : Scenario = null

    object Afters extends ScalaDsl with EN {
      After {  actualScenario = _ }
    }

    assertEquals(1, Afters.afterHooks.size)
    val hook = Afters.afterHooks.head
    assertTrue(hook.matches(List[Tag]().asJava))
    hook.execute(StubScenario)
    assertEquals(StubScenario, actualScenario)
  }

  @Test
  def taggedAfter {
    var actualScenario : Scenario = null

    object Afters extends ScalaDsl with EN {
      After("@foo,@bar", "@zap"){ actualScenario = _ }
    }

    assertEquals(1, Afters.afterHooks.size)

    val hook = Afters.afterHooks.head
    assertFalse(hook.matches(List[Tag]().asJava))
    assertTrue(hook.matches(List(new Tag("@bar", 0), new Tag("@zap", 0)).asJava))
    assertFalse(hook.matches(List(new Tag("@bar", 1)).asJava))

    hook.execute(StubScenario)
    assertEquals(StubScenario, actualScenario)
  }

  @Test
  def noArg {
    var called = false

    object Dummy extends ScalaDsl with EN {
      Given("x"){
        called = true
      }
    }

    assertEquals(1, Dummy.stepDefinitions.size)
    val step = Dummy.stepDefinitions.head
    assertEquals("ScalaDslTest.scala:127", step.getLocation(true)) // be careful with formatting or this test will break
    assertEquals("x", step.getPattern)
    step.execute(new I18n("en"), Array())
    assertTrue(called)
  }

  @Test
  def args {
    var thenumber = 0
    var thecolour = ""

    object Dummy extends ScalaDsl with EN {
      Given("Oh boy, (\\d+) (\\s+) cukes"){ (num:Int, colour:String) =>
        thenumber = num
        thecolour = colour
      }
    }

    assertEquals(1, Dummy.stepDefinitions.size)
    val step = Dummy.stepDefinitions(0)
    step.execute(new I18n("en"), Array(new java.lang.Integer(5), "green"))
    assertEquals(5, thenumber)
    assertEquals("green", thecolour)
  }

}
