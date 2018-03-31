package cucumber.api.testng;

import cucumber.runtime.CucumberException;
import cucumber.runtime.model.CucumberFeature;
import cucumber.runtime.testng.RunCukesStrict;
import cucumber.runtime.testng.RunCukesTest;
import cucumber.runtime.testng.RunScenarioWithUndefinedStepsStrict;
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
    public void runCukesStrict() throws Throwable {
        testNGCucumberRunner = new TestNGCucumberRunner(RunCukesStrict.class);

        for (Object[] scenario : testNGCucumberRunner.provideScenarios()) {
            PickleEventWrapper pickleEvent = (PickleEventWrapper) scenario[0];
            testNGCucumberRunner.runScenario(pickleEvent.getPickleEvent());
        }
    }

    @Test(expectedExceptions = CucumberException.class)
    public void runScenarioWithUndefinedStepsStrict() throws Throwable {
        testNGCucumberRunner = new TestNGCucumberRunner(RunScenarioWithUndefinedStepsStrict.class);
        Object[][] scenarios = testNGCucumberRunner.provideScenarios();

        // the feature file only contains one scenario
        Assert.assertEquals(scenarios.length, 1);
        Object[] scenario = scenarios[0];
        PickleEventWrapper pickleEvent = (PickleEventWrapper) scenario[0];

        testNGCucumberRunner.runScenario(pickleEvent.getPickleEvent()); // runScenario() throws CucumberException
    }

    @Test
    public void parse_error_propagated_to_testng_test_execution() throws Exception {
        testNGCucumberRunner = new ParseErrorCucumberRunner(RunCukesTest.class);
        Object[][] scenarios = testNGCucumberRunner.provideScenarios(); // a CucumberException is caught
        try {
            ((PickleEventWrapper)scenarios[0][0]).getPickleEvent();
            Assert.fail("CucumberException not thrown");
        } catch (CucumberException e) {
            Assert.assertEquals(e.getMessage(), "parse error");
        }
    }
}

class ParseErrorCucumberRunner extends TestNGCucumberRunner {
    public ParseErrorCucumberRunner(Class clazz) {
        super(clazz);
    }

    @Override
    List<CucumberFeature> getFeatures() {
        throw new CucumberException("parse error");
    }
}
