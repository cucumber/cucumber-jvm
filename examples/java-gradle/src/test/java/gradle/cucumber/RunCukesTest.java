package gradle.cucumber;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(format = {"pretty", "html:build/cucumber-html-report", "json-pretty:build/cucumber-report.json"})
public class RunCukesTest {

}
