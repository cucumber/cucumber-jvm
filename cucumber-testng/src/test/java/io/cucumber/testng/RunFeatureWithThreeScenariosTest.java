package io.cucumber.testng;

@SuppressWarnings({ "deprecation", "removal" })
@CucumberOptions(features = "classpath:io/cucumber/testng/three-scenarios.feature")
public class RunFeatureWithThreeScenariosTest extends AbstractTestNGCucumberTests {

}
