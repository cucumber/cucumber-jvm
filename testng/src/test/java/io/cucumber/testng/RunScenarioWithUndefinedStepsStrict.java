package io.cucumber.testng;

import io.cucumber.core.api.options.CucumberOptions;
import cucumber.api.testng.AbstractTestNGCucumberTests;

@CucumberOptions(
    features = "classpath:io/cucumber/testng/undefined_steps.feature",
    strict = true
)
public class RunScenarioWithUndefinedStepsStrict extends AbstractTestNGCucumberTests {
}
