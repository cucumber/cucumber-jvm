package io.cucumber.testng;

import cucumber.api.CucumberOptions;
import cucumber.api.testng.AbstractTestNGCucumberTests;

@CucumberOptions(
    features = "classpath:io/cucumber/testng/undefined_steps.feature",
    strict = true
)
public class RunScenarioWithUndefinedStepsStrict extends AbstractTestNGCucumberTests {
}
