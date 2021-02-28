package io.cucumber.spring.dirtiescontextconfig;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        glue = "io.cucumber.spring.dirtiescontextconfig",
        features = "classpath:io/cucumber/spring/dirtyCukes.feature")
public class RunCucumberTest {

}
