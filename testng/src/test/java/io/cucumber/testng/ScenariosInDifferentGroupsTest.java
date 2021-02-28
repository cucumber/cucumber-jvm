package io.cucumber.testng;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.function.Predicate;

@CucumberOptions(
        features = "classpath:io/cucumber/testng/scenarios-with-tags.feature",
        plugin = "timeline:target/timeline")
public class ScenariosInDifferentGroupsTest {

    private static final Predicate<Pickle> isSerial = pickle -> pickle.getTags().contains("@Serial");

    private TestNGCucumberRunner testNGCucumberRunner;

    @BeforeClass(alwaysRun = true)
    public void setUpClass() {
        testNGCucumberRunner = new TestNGCucumberRunner(this.getClass());
    }

    @Test(groups = "cucumber", description = "Runs Cucumber Scenarios", dataProvider = "parallelScenarios")
    public void runParallelScenario(PickleWrapper pickleWrapper, FeatureWrapper featureWrapper) {
        testNGCucumberRunner.runScenario(pickleWrapper.getPickle());
    }

    @DataProvider(parallel = true)
    public Object[][] parallelScenarios() {
        if (testNGCucumberRunner == null) {
            return new Object[0][0];
        }
        return filter(testNGCucumberRunner.provideScenarios(), isSerial.negate());
    }

    private Object[][] filter(Object[][] scenarios, Predicate<Pickle> accept) {
        return Arrays.stream(scenarios).filter(objects -> {
            PickleWrapper candidate = (PickleWrapper) objects[0];
            return accept.test(candidate.getPickle());
        }).toArray(Object[][]::new);
    }

    @Test(
            groups = "cucumber",
            description = "Runs Cucumber Scenarios in the Serial group",
            dataProvider = "serialScenarios")
    public void runSerialScenario(PickleWrapper pickleWrapper, FeatureWrapper featureWrapper) {
        testNGCucumberRunner.runScenario(pickleWrapper.getPickle());
    }

    @DataProvider
    public Object[][] serialScenarios() {
        if (testNGCucumberRunner == null) {
            return new Object[0][0];
        }

        return filter(testNGCucumberRunner.provideScenarios(), isSerial);
    }

    @AfterClass(alwaysRun = true)
    public void tearDownClass() {
        if (testNGCucumberRunner == null) {
            return;
        }
        testNGCucumberRunner.finish();
    }

}
