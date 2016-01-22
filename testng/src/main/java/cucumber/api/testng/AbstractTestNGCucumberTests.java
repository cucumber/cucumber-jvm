package cucumber.api.testng;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import cucumber.api.CucumberOptions;

/**
 * Runs cucumber every detected feature as separated test
 */
public abstract class AbstractTestNGCucumberTests {
    private TestNGCucumberRunner testNGCucumberRunner;
    private boolean failOnUnparseableFeatures = false;

    public AbstractTestNGCucumberTests() {
        CucumberOptions options = this.getClass().getAnnotation(CucumberOptions.class);
        if (options != null) {
            setFailOnUnparseableFeatures(options.strict());
        }
    }

    protected final void setFailOnUnparseableFeatures(boolean failOnUnparseableFeatures) {
        this.failOnUnparseableFeatures = failOnUnparseableFeatures;
    }

    @BeforeClass(alwaysRun = true)
    public void setUpClass() throws Exception {
        testNGCucumberRunner = new TestNGCucumberRunner(this.getClass());
    }

    @Test(groups = "cucumber", description = "Runs Cucumber Feature", dataProvider = "features")
    public void feature(CucumberFeatureWrapper cucumberFeature) {
        testNGCucumberRunner.runCucumber(cucumberFeature.getCucumberFeature());
    }

    @Test(groups = "cucumber", description = "Check if features files are parseable")
    public void featuresParseable() {
        if (failOnUnparseableFeatures) {
            this.features();
        }
    }

    /**
     * @return returns two dimensional array of {@link CucumberFeatureWrapper} objects.
     */
    @DataProvider
    public Object[][] features() {
        return testNGCucumberRunner.provideFeatures();
    }

    @AfterClass
    public void tearDownClass() throws Exception {
        testNGCucumberRunner.finish();
    }
}
