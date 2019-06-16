package cucumber.runtime.java.spring.metaconfig.general;

import cucumber.api.CucumberOptions;
import io.cucumber.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
    glue = {
        "cucumber.runtime.java.spring.metaconfig.general",
        "cucumber.runtime.java.spring.commonglue",
        "cucumber.api.spring"
    },
    features = {
        "classpath:cucumber/runtime/java/spring/springBeanInjectionWithMetaConfiguration.feature"
    }
)
public class RunCucumberTest {
}
