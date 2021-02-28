package io.cucumber.examples.java8;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(plugin = "message:target/cucumber-report.ndjson")
public class RunCucumberTest {

}
