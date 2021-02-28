package io.cucumber.spring.metaconfig.dirties;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        glue = "io.cucumber.spring.metaconfig.dirties",
        features = "classpath:io/cucumber/spring/dirtyCukesWithMetaConfiguration.feature")
public class RunCucumberTest {

}
