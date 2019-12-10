package io.cucumber.examples.java;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(plugin = {"message:target/results.bin", "message:target/results.ndjson"})
public class RunCucumberTest {

}
