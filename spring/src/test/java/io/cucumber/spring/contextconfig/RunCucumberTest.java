package io.cucumber.spring.contextconfig;

import io.cucumber.core.api.options.CucumberOptions;
import io.cucumber.junit.api.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
    glue = {
        "io.cucumber.spring.contextconfig",
        "io.cucumber.spring.commonglue",
        "io.cucumber.spring.api"
    },
    features = {
        "classpath:io/cucumber/spring/stepdefInjection.feature",
        "classpath:io/cucumber/spring/transaction.feature"
    }
)
public class RunCucumberTest {
}
