package cucumber.api.testng;

import cucumber.runtime.model.CucumberFeature;
import cucumber.runtime.testng.RunCukesTest;
import org.testng.*;
import org.testng.annotations.*;

import java.util.List;

public class TestNGCucumberRunnerTest {
    private TestNGCucumberRunner testNGCucumberRunner;

    @BeforeMethod
    public void setUp() throws Exception {
        testNGCucumberRunner = new TestNGCucumberRunner(RunCukesTest.class);
    }

    @Test
    public void getFeatures() throws Exception {
        List<CucumberFeature> features = testNGCucumberRunner.getFeatures();

        Assert.assertFalse(features.isEmpty());
        Assert.assertEquals(features.size(), 3, "All features associated with RunCukesTest are loaded");
    }
}
