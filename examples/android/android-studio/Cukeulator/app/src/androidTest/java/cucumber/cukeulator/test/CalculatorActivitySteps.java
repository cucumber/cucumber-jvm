package cucumber.cukeulator.test;

import android.app.Activity;
import android.support.test.rule.ActivityTestRule;

import org.junit.Rule;

import cucumber.api.CucumberOptions;
import cucumber.api.java.After;
import cucumber.api.java.Before;
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
import static org.junit.Assert.assertNotNull;

/**
 * We use {@link ActivityTestRule} in order to have access to methods like getActivity
 * and getInstrumentation.
 * </p>
 * The CucumberOptions annotation is mandatory for exactly one of the classes in the test project.
 * Only the first annotated class that is found will be used, others are ignored. If no class is
 * annotated, an exception is thrown.
 * <p/>
 * The options need to at least specify features = "features". Features must be placed inside
 * assets/features/ of the test project (or a subdirectory thereof).
 */
@CucumberOptions
public class CalculatorActivitySteps {

    public CalculatorActivitySteps(SomeDependency dependency) {
        assertNotNull(dependency);
    }

    /**
    * Since {@link CucumberRunner} and {@link cucumber.api.android.CucumberInstrumentationCore} have the control over the
    * test lifecycle, activity test rules must not be launched automatically. Automatic launching of test rules is only
    * feasible for JUnit tests. Fortunately, we are able to launch the activity in Cucumber's {@link Before} method.
    */
    @Rule
    ActivityTestRule rule = new ActivityTestRule<>(CalculatorActivity.class, false, false);

    /**
     * We launch the activity in Cucumber's {@link Before} hook.
     * See the notes above for the reasons why we are doing this.
     * @throws Exception any possible Exception
     */
    @Before
    public void launchActivity() throws Exception {
        rule.launchActivity(null);
    }

    /**
     * All the clean up of application's data and state after each scenario must happen here
     */
    @After
    public void finishActivity() throws Exception {
        getActivity().finish();
    }

    /**
     * Gets the activity from our test rule.
     * @return the activity
     */
    private Activity getActivity() {
        return rule.getActivity();
    }

    @Given("I have a CalculatorActivity")
    public void I_have_a_CalculatorActivity() {
        assertNotNull(getActivity());
    }

    @When("I press {digit}")
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

    @When("I press {operator}")
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

    @Then("I should see {string} on the display")
    public void I_should_see_s_on_the_display(final String s) {
        onView(withId(R.id.txt_calc_display)).check(matches(withText(s)));
    }
}
