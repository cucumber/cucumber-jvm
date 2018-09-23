package io.cucumber.testng.api;

import io.cucumber.core.api.options.CucumberOptions;
import io.cucumber.testng.api.AbstractTestNGCucumberTests;

@CucumberOptions(
    features = "classpath:io/cucumber/testng/api/three_scenarios.feature"
)
public class RunFeatureWithThreeScenariosTest extends AbstractTestNGCucumberTests {
}
