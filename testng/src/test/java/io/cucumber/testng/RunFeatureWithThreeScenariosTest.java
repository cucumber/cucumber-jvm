package io.cucumber.testng;

import io.cucumber.core.api.options.CucumberOptions;
import cucumber.api.testng.AbstractTestNGCucumberTests;

@CucumberOptions(
    features = "classpath:io/cucumber/testng/three_scenarios.feature"
)
public class RunFeatureWithThreeScenariosTest extends AbstractTestNGCucumberTests {
}
