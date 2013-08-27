package cucumber.example.android.cukeulator.test;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.TextView;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import cucumber.example.android.cukeulator.CalculatorActivity;
import cucumber.example.android.cukeulator.R;

import static cucumber.example.android.cukeulator.test.Utils.clickOnView;

public class CalculatorActivitySteps extends ActivityInstrumentationTestCase2<CalculatorActivity> {
    private CalculatorActivity activity;

    public CalculatorActivitySteps() {
        super(CalculatorActivity.class);
    }

    @Before
    public void before() {
    }

    @After
    public void after() {
    }

    @Given("^I have a CalculatorActivity$")
    public void I_have_a_CalculatorActivity() {
        activity = getActivity();
        assertNotNull(activity);
    }

    @When("^I press (\\d)$")
    public void I_press_d(int d) {
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
        TextView display = (TextView) activity.findViewById(R.id.txt_calc_display);
        String displayed_result = display.getText().toString();
        assertEquals(s, displayed_result);
    }
}
