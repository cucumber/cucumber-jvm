package io.cucumber.examples.testng;

import io.cucumber.junit.CucumberOptions;
import io.cucumber.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(plugin = {"progress", "summary", "json:target/cucumber-report.json"})
public class RunCucumberTest {

}
