package cucumber.api.testng;

import cucumber.runtime.CucumberException;
import cucumber.runtime.testng.RunCukesStrict;
import org.testng.annotations.Test;

public class TestNGCucumberRunnerTest {

    @Test(expectedExceptions = CucumberException.class)
    public void runCukesStrict() throws Exception {
        TestNGCucumberRunner testNGCucumberRunner = new TestNGCucumberRunner(RunCukesStrict.class);
        testNGCucumberRunner.runCukes();
    }

}
