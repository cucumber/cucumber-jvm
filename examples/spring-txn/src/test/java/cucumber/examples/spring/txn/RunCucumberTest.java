package cucumber.examples.spring.txn;

import io.cucumber.junit.CucumberOptions;
import io.cucumber.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(glue = {"cucumber.examples.spring.txn", "cucumber.api.spring"})
public class RunCucumberTest {
}
