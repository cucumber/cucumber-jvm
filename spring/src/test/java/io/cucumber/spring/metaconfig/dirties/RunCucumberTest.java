package io.cucumber.spring.metaconfig.dirties;

import cucumber.api.CucumberOptions;
import io.cucumber.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
    glue = {"io.cucumber.spring.metaconfig.dirties"},
    features = {"classpath:io/cucumber/spring/dirtyCukesWithMetaConfiguration.feature"}
)
public class RunCucumberTest {
}
