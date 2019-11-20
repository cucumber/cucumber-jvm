package cucumber.runtime.java.spring.mockbeans;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
    glue = {"cucumber.runtime.java.spring.mockbeans"},
    features = {"classpath:cucumber/runtime/java/spring/mockbeans.feature"}
)
public class RunCucumberTest {
}
