package cucumber.runtime.testng;

import cucumber.api.CucumberOptions;
import cucumber.api.testng.AbstractTestNGCucumberTests;

@CucumberOptions(
    features = "classpath:cucumber/runtime/testng/undefined_steps.feature",
    strict = true
)
public class RunScenarioWithUndefinedStepsStrict extends AbstractTestNGCucumberTests {
}
