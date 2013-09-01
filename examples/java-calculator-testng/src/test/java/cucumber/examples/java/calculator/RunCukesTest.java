package cucumber.examples.java.calculator;

import cucumber.api.CucumberOptions;
import cucumber.api.testng.AbstractTestNGCucumberTests;

@CucumberOptions(format = "json:target/cucumber-report.json")
public class RunCukesTest extends AbstractTestNGCucumberTests {
}
