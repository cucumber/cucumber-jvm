package cucumber.runtime.scala.subcut

import _root_.org.junit.{ Test, Assert }
import Assert._
import cucumber.runtime.CucumberException
import com.escalatesoft.subcut.inject.NewBindingModule
import com.escalatesoft.subcut.inject.BindingModule
import com.escalatesoft.subcut.inject.Injectable
import subcut.loadmodule.SubCutConfigurationModule
import subcut.loadmodule.SubCutConfigurationModule

class SubCutObjectFactoryTest {

  val factory = new SubCutObjectFactory

  @Test
  def testInstantiateWithBindingModule {
    val module = factory.getInstance(classOf[WithBindingModule]).bindingModule
    assertEquals(module.getClass().getSimpleName(), "SubCutConfigurationModule$")
  }

  @Test
  def testInstantiateNoBindingModule {
    val instance = factory.getInstance(classOf[NoBindingModule])
    assertNotNull(instance)
  }

}

class WithBindingModule(implicit val bindingModule: BindingModule) extends Injectable
class NoBindingModule 