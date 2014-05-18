package cucumber.runtime.java.spring_dirtiescontextconfig;

import cucumber.api.CucumberOptions;

import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(glue = {"cucumber.runtime.java.spring_dirtiescontextconfig"},
        features = {"classpath:cucumber/runtime/java/spring/dirtyCukes.feature"})
public class RunCukesTest {
}
