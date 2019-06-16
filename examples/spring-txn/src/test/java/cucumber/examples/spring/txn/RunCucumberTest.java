package cucumber.examples.spring.txn;

import cucumber.api.CucumberOptions;
import io.cucumber.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(glue = {"cucumber.examples.spring.txn", "cucumber.api.spring"})
public class RunCucumberTest {
}
