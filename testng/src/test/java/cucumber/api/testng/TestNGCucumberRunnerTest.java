package cucumber.api.testng;

import cucumber.runtime.CucumberException;
import cucumber.runtime.model.CucumberFeature;
import cucumber.runtime.model.CucumberTagStatement;
import cucumber.runtime.testng.RunCukesStrict;
import cucumber.runtime.testng.RunCukesTest;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.util.List;

public class TestNGCucumberRunnerTest {
    private TestNGCucumberRunner testNGCucumberRunner;

    @BeforeMethod
    public void setUp() throws Exception {
        testNGCucumberRunner = new TestNGCucumberRunner(RunCukesTest.class);
    }

    @Test(expectedExceptions = CucumberException.class)
    public void runCukesStrict() throws Exception {
        testNGCucumberRunner = new TestNGCucumberRunner(RunCukesStrict.class);
        testNGCucumberRunner.runCukes();
    }

    @Test
    public void parse_error_propagated_to_testng_test_execution() throws Exception {
        testNGCucumberRunner = new ParseErrorCucumberRunner(RunCukesTest.class);
        Object[][] features = testNGCucumberRunner.provideFeatures(); // provideFeatures() throws CucumberException
        try {
            ((CucumberFeatureWrapper)features[0][0]).getCucumberFeature();
            Assert.fail("CucumberException not thrown");
        } catch (CucumberException e) {
            Assert.assertEquals(e.getMessage(), "parse error");
        }
    }

    @Test
    public void getFeatures() throws Exception {
        List<CucumberFeature> features = testNGCucumberRunner.getFeatures();

        int numberOfFeatures = getNumberOfFeatures();

        Assert.assertEquals(features.size(), numberOfFeatures,
                "Not all features associated with " + RunCukesTest.class.getSimpleName() + " were loaded. ");
        Assert.assertTrue(!features.isEmpty(), "Feature files need to exist in the cucumber/runtime/testng/ folder for this test");
    }

    @Test
    public void getScenarios() throws Exception {
        List<CucumberTagStatement> scenarios = testNGCucumberRunner.getScenarios();

        int numberOfFeatures = getNumberOfFeatures();

        Assert.assertTrue(scenarios.size() >= numberOfFeatures,
                "Not all features associated with " + RunCukesTest.class.getSimpleName() + " were loaded. ");
        Assert.assertTrue(!scenarios.isEmpty(), "Feature files need to exist in the cucumber/runtime/testng/ folder for this test");
    }

    /**
     * @return number of feature files in "cucumber/runtime/testng/" folder
     */
    private int getNumberOfFeatures() {
        URL fileURL = this.getClass().getClassLoader().getResource("cucumber/runtime/testng/");
        assert fileURL != null;
        return new File(fileURL.getFile()).listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".feature");
            }
        }).length;
    }
}

class ParseErrorCucumberRunner extends TestNGCucumberRunner {
    public ParseErrorCucumberRunner(Class clazz) {
        super(clazz);
    }

    @Override
    public List<CucumberFeature> getFeatures() {
        throw new CucumberException("parse error");
    }
}
