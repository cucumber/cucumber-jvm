package cucumber.api.testng;

import cucumber.runtime.SingleFeatureBuilder;
import cucumber.runtime.io.Resource;
import cucumber.runtime.model.CucumberFeature;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.Iterator;

/**
 * Runs cucumber every detected feature as separated test
 */
public abstract class AbstractTestNGLazyLoadCucumberTests {
    private TestNGOneCucumberFeatureRunner testNGLazyLoadingCucumberRunner;
    private SingleFeatureBuilder singleFeatureBuilder ;
    @BeforeClass(alwaysRun = true)
    public void setUpClass() throws Exception {
        testNGLazyLoadingCucumberRunner = new TestNGOneCucumberFeatureRunner(this.getClass());
        singleFeatureBuilder = new SingleFeatureBuilder();
    }

    @Test(groups = "cucumber", description = "Runs Cucumber Feature one by one", dataProvider = "featuresIterator")
    public void feature(Resource cucumberFeatureResource) {
        singleFeatureBuilder.parse(cucumberFeatureResource, Collections.emptyList());
        CucumberFeature cucumberFeature = singleFeatureBuilder.getCurrentCucumberFeature();
        testNGLazyLoadingCucumberRunner.runCucumber(cucumberFeature);
    }

    /**
     * @return Iterator of array of objects
     */
    @DataProvider
    public Iterator<Object[]> featuresIterator() {
        return testNGLazyLoadingCucumberRunner.getFeatureResources();
    }

    @AfterClass
    public void tearDownClass() throws Exception {
        testNGLazyLoadingCucumberRunner.finish();
    }
}
