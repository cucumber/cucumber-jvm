package io.cucumber.spring.contextconfig;

import io.cucumber.junit.CucumberOptions;
import io.cucumber.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
    glue = {
        "io.cucumber.spring.contextconfig",
        "io.cucumber.spring.commonglue",
        "cucumber.api.spring"
    },
    features = {
        "classpath:io/cucumber/spring/stepdefInjection.feature",
        "classpath:io/cucumber/spring/transaction.feature"
    }
)
public class RunCucumberTest {
}
