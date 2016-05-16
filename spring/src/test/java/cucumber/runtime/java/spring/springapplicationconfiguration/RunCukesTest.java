package cucumber.runtime.java.spring.springapplicationconfiguration;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(glue = {"cucumber.runtime.java.spring.springapplicationconfiguration"},
        features = {"classpath:cucumber/runtime/java/spring/springBeanInjectionBoot.feature"})
public class RunCukesTest {
}
