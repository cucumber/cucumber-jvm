package io.cucumber.spring.metaconfig.dirties;

import io.cucumber.core.api.options.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(glue = {"cucumber.runtime.java.spring.metaconfig.dirties"},
        features = {"classpath:io/cucumber/spring/dirtyCukesWithMetaConfiguration.feature"})
public class RunCukesTest {
}
