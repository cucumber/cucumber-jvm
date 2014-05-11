package cucumber.runtime.java.spring_webappconfig;

import cucumber.api.CucumberOptions;

import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(glue = {"cucumber.runtime.java.spring_webappconfig"},
        features = {"classpath:cucumber/runtime/java/spring/springinjection.feature"})
public class RunCukesTest {
}
