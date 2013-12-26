package cucumber.api.testng;

import cucumber.runtime.CucumberException;
import cucumber.runtime.model.CucumberFeature;
import cucumber.runtime.testng.RunCukesStrict;
import cucumber.runtime.testng.RunCukesTest;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

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
    public void getFeatures() throws Exception {
        List<CucumberFeature> features = testNGCucumberRunner.getFeatures();

        Assert.assertFalse(features.isEmpty());
        Assert.assertEquals(features.size(), 3, "All features associated with RunCukesTest are loaded");
    }
}
