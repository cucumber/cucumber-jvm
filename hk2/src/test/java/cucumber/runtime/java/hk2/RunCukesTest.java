package cucumber.runtime.java.hk2;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(glue = "cucumber.runtime.java.hk2.integrationTest")
public class RunCukesTest {
}
