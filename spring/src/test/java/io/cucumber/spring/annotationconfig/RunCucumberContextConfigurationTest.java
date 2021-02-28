package io.cucumber.spring.annotationconfig;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        glue = "io.cucumber.spring.annotationconfig",
        features = "classpath:io/cucumber/spring/annotationContextConfiguration.feature")
public class RunCucumberContextConfigurationTest {

}
