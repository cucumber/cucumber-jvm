package cucumber.api.testng;

import cucumber.runtime.CucumberException;
import cucumber.runtime.model.CucumberFeature;
import cucumber.runtime.testng.RunCukesStrict;
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
        testNGCucumberRunner = new TestNGCucumberRunner(BatmanTest.class);
    }

    @Test(expectedExceptions = CucumberException.class)
    public void runCukesStrict() throws Exception {
        testNGCucumberRunner = new TestNGCucumberRunner(RunCukesStrict.class);
        testNGCucumberRunner.runCukes();
    }

    @Test
    public void getFeatures() throws Exception {
        List<CucumberFeature> features = testNGCucumberRunner.getFeatures();

        int numberOfFeatures = getNumberOfFeatures();

        Assert.assertEquals(features.size(), numberOfFeatures,
                "Not all features associated with " + BatmanTest.class.getSimpleName() + " were loaded. ");
    }

    /**
     * @return number of feature files in "cucumber/api/testng/batman" folder
     */
    private int getNumberOfFeatures() {
        URL fileURL = this.getClass().getClassLoader().getResource("cucumber/api/testng/batman");
        assert fileURL != null;
        return new File(fileURL.getFile()).listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".feature");
            }
        }).length;
    }
}
