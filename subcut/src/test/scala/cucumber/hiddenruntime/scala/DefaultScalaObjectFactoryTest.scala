package cucumber.hiddenruntime.scala

import _root_.org.junit.{Test, Assert}
import Assert._
import cucumber.runtime.CucumberException

class DefaultScalaObjectFactoryTest {

  val factory = new DefaultScalaObjectFactory
  @Test
  def testGetInstance{
    val instance1 = factory.getInstance(classOf[String])
    val instance2 = factory.getInstance(classOf[String])
    assert(instance1.eq(instance2))
  }
  
  @Test(expected=classOf[CucumberException])
  def testNoDefaultConstructor{
    factory.getInstance(classOf[NoDefaultConstructor])
  }
}

class NoDefaultConstructor(param:String)