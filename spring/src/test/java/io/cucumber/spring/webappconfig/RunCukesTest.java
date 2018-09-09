package io.cucumber.spring.webappconfig;

import io.cucumber.core.api.options.CucumberOptions;

import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(glue = {"cucumber.runtime.java.spring.webappconfig"},
        features = {"classpath:io/cucumber/spring/springWebContextInjection.feature"})
public class RunCukesTest {
}
