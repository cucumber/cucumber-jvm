package cucumber.cukeulator.test;

import android.test.ActivityInstrumentationTestCase2;
import cucumber.api.CucumberOptions;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import cucumber.cukeulator.CalculatorActivity;
import cucumber.cukeulator.R;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

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
 * The options need to at least specify features = "features". Features must be placed inside
 * assets/features/ of the test project (or a subdirectory thereof).
 */
@CucumberOptions(features = "features")
public class CalculatorActivitySteps extends ActivityInstrumentationTestCase2<CalculatorActivity> {

    public CalculatorActivitySteps(SomeDependency dependency) {
        super(CalculatorActivity.class);
        assertNotNull(dependency);
    }

    @Given("^I have a CalculatorActivity$")
    public void I_have_a_CalculatorActivity() {
        assertNotNull(getActivity());
    }

    @When("^I press (\\d)$")
    public void I_press_d(final int d) {
        switch (d) {
            case 0:
                onView(withId(R.id.btn_d_0)).perform(click());
                break;
            case 1:
                onView(withId(R.id.btn_d_1)).perform(click());
                break;
            case 2:
                onView(withId(R.id.btn_d_2)).perform(click());
                break;
            case 3:
                onView(withId(R.id.btn_d_3)).perform(click());
                break;
            case 4:
                onView(withId(R.id.btn_d_4)).perform(click());
                break;
            case 5:
                onView(withId(R.id.btn_d_5)).perform(click());
                break;
            case 6:
                onView(withId(R.id.btn_d_6)).perform(click());
                break;
            case 7:
                onView(withId(R.id.btn_d_7)).perform(click());
                break;
            case 8:
                onView(withId(R.id.btn_d_8)).perform(click());
                break;
            case 9:
                onView(withId(R.id.btn_d_9)).perform(click());
                break;
        }
    }

    @When("^I press ([+–x\\/=])$")
    public void I_press_op(final char op) {
        switch (op) {
            case '+':
                onView(withId(R.id.btn_op_add)).perform(click());
                break;
            case '–':
                onView(withId(R.id.btn_op_subtract)).perform(click());
                break;
            case 'x':
                onView(withId(R.id.btn_op_multiply)).perform(click());
                break;
            case '/':
                onView(withId(R.id.btn_op_divide)).perform(click());
                break;
            case '=':
                onView(withId(R.id.btn_op_equals)).perform(click());
                break;
        }
    }

    @Then("^I should see (\\S+) on the display$")
    public void I_should_see_s_on_the_display(final String s) {
        onView(withId(R.id.txt_calc_display)).check(matches(withText(s)));
    }
}
