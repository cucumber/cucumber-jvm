package cucumber.android.test;

import android.app.Instrumentation;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;
import cucumber.api.android.CucumberInstrumentation;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

// Glue code classes need to extend Android test classes
// in order to have access to Context and Instrumentation.
public class CucumberActivitySteps extends ActivityInstrumentationTestCase2<CucumberActivity> {
    private int steps;

    public CucumberActivitySteps() {
        super(CucumberActivity.class);
    }

    @Before
    public void before() {
        Log.d(CucumberInstrumentation.TAG, "android-test before");
        assertEquals(0, steps);
        Instrumentation instrumentation = getInstrumentation();
        assertNotNull(instrumentation);
        assertNotNull(getActivity());
        String testPackageName = instrumentation.getContext().getPackageName();
        String targetPackageName = instrumentation.getContext().getPackageName();
        assertEquals(testPackageName, targetPackageName);
    }

    @After
    public void after() {
        Log.d(CucumberInstrumentation.TAG, "android-test after");
        assertEquals(3, steps);
    }

    @Given("^I have a test$")
    public void I_have_a_test() {
        assertEquals(1, ++steps);
    }

    @When("^I test$")
    public void I_test() {
        assertEquals(2, ++steps);
    }

    @Then("^I succeed$")
    public void I_succeed() {
        assertEquals(3, ++steps);
    }
}
