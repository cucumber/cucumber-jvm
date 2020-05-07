package io.cucumber.spring.contextconfig;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        glue = {
                "io.cucumber.spring.contextconfig",
                "io.cucumber.spring.commonglue",
                "io.cucumber.spring.api"
        },
        features = "classpath:io/cucumber/spring/stepdefInjection.feature")
public class RunCucumberTest {

}
