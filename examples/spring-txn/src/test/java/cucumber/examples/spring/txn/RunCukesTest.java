package cucumber.examples.spring.txn;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(glue = {"cucumber.examples.spring.txn", "cucumber.runtime.java.spring.hooks"})
public class RunCukesTest {
}
