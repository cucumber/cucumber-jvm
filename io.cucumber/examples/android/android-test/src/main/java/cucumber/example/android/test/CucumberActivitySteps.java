package cucumber.example.android.test;

import android.app.Instrumentation;
import android.test.ActivityInstrumentationTestCase2;
import cucumber.api.CucumberOptions;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

/**
 * We extend ActivityInstrumentationTestCase2 in order to have access to methods like getActivity
 * and getInstrumentation. Depending on what methods we are going to need, we can put our
 * step definitions inside classes extending any of the following Android test classes:
 * <p/>
 * ActivityInstrumentationTestCase2
 * InstrumentationTestCase
 * AndroidTestCase
 * <p/>
 * The CucumberOptions annotation is mandatory for exactly one of the classes in the test project.
 * Only the first annotated class that is found will be used, others are ignored. If no class is
 * annotated, an exception is thrown.
 * <p/>
 * The options need to at least specify features = "features". The default value that is set by
 * Cucumber internally does not work because features are not on the classpath under Android.
 * Features must be placed inside assets/features/ of the test project (or a subdirectory thereof).
 */
@CucumberOptions(features = "features")
public class CucumberActivitySteps extends ActivityInstrumentationTestCase2<CucumberActivity> {
    private int steps;

    public CucumberActivitySteps(SomeDependency dependency) {
        super(CucumberActivity.class);
        assertNotNull(dependency);
    }

    @Before
    public void before() {
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
