package cucumber.examples.java.calculator;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(plugin = {"progress", "json:target/cucumber-report.json"})
@Category(Example.class)
public class RunCukesTest {

}
