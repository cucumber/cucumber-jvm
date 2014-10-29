package cucumber.runtime.java.spring.metaconfig;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(glue = {"cucumber.runtime.java.spring.contextconfig",
                "cucumber.runtime.java.spring.commonglue",
                "cucumber.api.spring"},
        features = {"classpath:cucumber/runtime/java/spring/springBeanInjectionWithMetaConfiguration.feature"})
public class RunCukesTest {
}
