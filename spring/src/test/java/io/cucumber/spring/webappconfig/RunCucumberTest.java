package io.cucumber.spring.webappconfig;

import io.cucumber.core.api.options.CucumberOptions;
import io.cucumber.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
    glue = {"io.cucumber.spring.webappconfig"},
    features = {"classpath:io/cucumber/spring/springWebContextInjection.feature"}
)
public class RunCucumberTest {
}
