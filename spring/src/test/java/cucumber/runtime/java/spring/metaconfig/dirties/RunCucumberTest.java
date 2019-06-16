package cucumber.runtime.java.spring.metaconfig.dirties;

import cucumber.api.CucumberOptions;
import io.cucumber.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
    glue = {"cucumber.runtime.java.spring.metaconfig.dirties"},
    features = {"classpath:cucumber/runtime/java/spring/dirtyCukesWithMetaConfiguration.feature"}
)
public class RunCucumberTest {
}
