package io.cucumber.testng;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

@CucumberOptions(
    features = "classpath:io/cucumber/testng/scenarios_with_tags.feature"
)
public class TestNGCucumberTestsWithExtraDataProvider extends AbstractTestNGCucumberTests {

    @Test(groups = "cucumber", description = "Runs Cucumber Scenarios", dataProvider = "tag2Scenarios")
    public void runTag2Scenario(PickleWrapper pickleWrapper, FeatureWrapper featureWrapper) throws Throwable {
        super.runScenario(pickleWrapper, featureWrapper);
    }

    @DataProvider
    public Object[][] tag2Scenarios() {
        return super.scenarios();
    }

}
