package io.cucumber.spring.dirtiescontextconfig;

import io.cucumber.core.api.options.CucumberOptions;

import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(glue = {"io.cucumber.spring.dirtiescontextconfig"},
        features = {"classpath:io/cucumber/spring/dirtyCukes.feature"})
public class RunCukesTest {
}
