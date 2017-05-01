package cucumber.api.testng;

import cucumber.runtime.model.CucumberTagStatement;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Runs cucumber every detected feature as separated test
 */
public abstract class AbstractTestNGCucumberTests {
    private TestNGCucumberRunner testNGCucumberRunner;

    @BeforeClass(alwaysRun = true)
    public void setUpClass() throws Exception {
        testNGCucumberRunner = new TestNGCucumberRunner(this.getClass());
    }

    @Test(groups = "cucumber", description = "Runs Cucumber Feature", dataProvider = "features")
    public void feature(CucumberFeatureWrapper cucumberFeature) {
        testNGCucumberRunner.runCucumber(cucumberFeature.getCucumberFeature());
    }

    @Test(groups = "cucumber-scenarios", description = "Runs Cucumber Scenarios", dataProvider = "scenarios")
    public void scenario(CucumberTagStatementWrapper cucumberTagStatement) throws Throwable {
        testNGCucumberRunner.runCucumberScenario(cucumberTagStatement.getCucumberScenario());
    }

    /**
     * @return returns two dimensional array of {@link CucumberFeatureWrapper} objects.
     */
    @DataProvider
    public Object[][] features() {
        return testNGCucumberRunner.provideFeatures();
    }

    /**
     * @return returns two dimensional array of {@link CucumberTagStatement} objects with the
     * scenario name.
     */
    @DataProvider
    public Object[][] scenarios() {
        return testNGCucumberRunner.provideScenarios();
    }

    @AfterClass(alwaysRun = true)
    public void tearDownClass() throws Exception {
        testNGCucumberRunner.finish();
    }
}
