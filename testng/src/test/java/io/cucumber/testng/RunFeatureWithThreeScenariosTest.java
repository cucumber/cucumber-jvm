package io.cucumber.testng;

import cucumber.api.CucumberOptions;
import cucumber.api.testng.AbstractTestNGCucumberTests;

@CucumberOptions(
    features = "classpath:io/cucumber/testng/three_scenarios.feature"
)
public class RunFeatureWithThreeScenariosTest extends AbstractTestNGCucumberTests {
}
