package io.cucumber.testng;


@CucumberOptions(
    features = "classpath:io/cucumber/testng/three_scenarios.feature"
)
public class RunFeatureWithThreeScenariosTest extends AbstractTestNGCucumberTests {
}
