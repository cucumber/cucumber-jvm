package io.cucumber.examples.java;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(plugin = {"protobuf:target/results.bin", "protobuf:target/results.ndjson"})
public class RunCucumberTest {

}
