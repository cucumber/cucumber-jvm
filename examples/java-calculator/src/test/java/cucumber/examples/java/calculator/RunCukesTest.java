package cucumber.examples.java.calculator;

import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@Cucumber.Options(format = "json:target/cucumber-report.json")
public class RunCukesTest {
}
