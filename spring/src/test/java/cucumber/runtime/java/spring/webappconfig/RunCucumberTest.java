package cucumber.runtime.java.spring.webappconfig;

import cucumber.api.CucumberOptions;
import io.cucumber.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
    glue = {"cucumber.runtime.java.spring.webappconfig"},
    features = {"classpath:cucumber/runtime/java/spring/springWebContextInjection.feature"}
)
public class RunCucumberTest {
}
