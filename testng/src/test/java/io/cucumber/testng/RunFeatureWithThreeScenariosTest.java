package io.cucumber.testng;

import io.cucumber.core.api.options.CucumberOptions;

@CucumberOptions(
    features = "classpath:io/cucumber/testng/three_scenarios.feature"
)
public class RunFeatureWithThreeScenariosTest extends AbstractTestNGCucumberTests {
}
