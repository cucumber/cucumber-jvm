package subcut.loadmodule

import org.junit.runner.RunWith
import cucumber.api.junit.Cucumber;

@RunWith(classOf[Cucumber])
@Cucumber.Options(
		glue=Array("subcut.loadmodule"),
		strict = true,
		features = Array("classpath:loadmodule/")) 
class RunSubCutTest {
  
}
