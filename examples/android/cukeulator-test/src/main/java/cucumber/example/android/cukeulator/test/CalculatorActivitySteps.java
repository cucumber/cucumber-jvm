package cucumber.example.android.cukeulator.test;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.TextView;
import cucumber.api.CucumberOptions;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import cucumber.example.android.cukeulator.CalculatorActivity;
import cucumber.example.android.cukeulator.R;

import static cucumber.example.android.cukeulator.test.Utils.clickOnView;

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
    public void I_press_d(int d) {
        CalculatorActivity activity = getActivity();

        switch (d) {
            case 0:
                clickOnView(activity, R.id.btn_d_0);
                break;
            case 1:
                clickOnView(activity, R.id.btn_d_1);
                break;
            case 2:
                clickOnView(activity, R.id.btn_d_2);
                break;
            case 3:
                clickOnView(activity, R.id.btn_d_3);
                break;
            case 4:
                clickOnView(activity, R.id.btn_d_4);
                break;
            case 5:
                clickOnView(activity, R.id.btn_d_5);
                break;
            case 6:
                clickOnView(activity, R.id.btn_d_6);
                break;
            case 7:
                clickOnView(activity, R.id.btn_d_7);
                break;
            case 8:
                clickOnView(activity, R.id.btn_d_8);
                break;
            case 9:
                clickOnView(activity, R.id.btn_d_9);
                break;
        }
    }

    @When("^I press ([+–x\\/=])$")
    public void I_press_op(char op) {
        CalculatorActivity activity = getActivity();

        switch (op) {
            case '+':
                clickOnView(activity, R.id.btn_op_add);
                break;
            case '–':
                clickOnView(activity, R.id.btn_op_subtract);
                break;
            case 'x':
                clickOnView(activity, R.id.btn_op_multiply);
                break;
            case '/':
                clickOnView(activity, R.id.btn_op_divide);
                break;
            case '=':
                clickOnView(activity, R.id.btn_op_equals);
                break;
        }
    }

    @Then("^I should see (\\S+) on the display$")
    public void I_should_see_s_on_the_display(String s) {
        TextView display = (TextView) getActivity().findViewById(R.id.txt_calc_display);
        String displayed_result = display.getText().toString();
        assertEquals(s, displayed_result);
    }
}
