package cucumber.runtime.java.spring.dirtiescontextconfig;

import cucumber.api.CucumberOptions;
import io.cucumber.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
    glue = {"cucumber.runtime.java.spring.dirtiescontextconfig"},
    features = {"classpath:cucumber/runtime/java/spring/dirtyCukes.feature"}
)
public class RunCucumberTest {
}
