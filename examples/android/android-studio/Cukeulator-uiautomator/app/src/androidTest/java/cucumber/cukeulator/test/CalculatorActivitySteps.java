package cucumber.cukeulator.test;

import android.content.Context;
import android.content.Intent;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.SearchCondition;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiSelector;
import android.support.test.uiautomator.Until;
import android.test.InstrumentationTestCase;

import cucumber.api.CucumberOptions;
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
 * The options need to at least specify features = "features". Features must be placed inside
 * assets/features/ of the test project (or a subdirectory thereof).
 */
@CucumberOptions(features = "features")
public class CalculatorActivitySteps extends InstrumentationTestCase {
    private UiDevice mDevice;
    private Context mAppContext;


    protected String getAppPackage() {
        return "cucumber.cukeulator";
    }

    protected String id(String id) {
        return getAppPackage() + ":id/" + id;
    }

    protected long getAppTimeout() {
        return 10000;
    }

    public CalculatorActivitySteps(SomeDependency dependency) {
        assertNotNull(dependency);
    }

    protected void wait(SearchCondition<Boolean> until) {
        wait(until, getAppTimeout());
    }

    protected void wait(SearchCondition<Boolean> until, long timeout) {
        assertTrue(String.valueOf(until), mDevice.wait(until, timeout));
    }

    @Given("^I have a CalculatorActivity$")
    public void I_have_a_CalculatorActivity() {
        mDevice = UiDevice.getInstance(getInstrumentation());
        mAppContext = getInstrumentation().getContext();

        Intent intent = mAppContext.getPackageManager().getLaunchIntentForPackage(getAppPackage());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        mAppContext.startActivity(intent);

        wait(Until.hasObject(By.pkg(getAppPackage()).depth(0)));
    }

    @When("^I press (\\d)$")
    public void I_press_d(final int d) throws UiObjectNotFoundException {
        assertTrue(d >= 0);
        assertTrue(d <= 9);

        UiObject button = mDevice.findObject(new UiSelector().resourceId(id("btn_d_" + d)));
        assertEquals(button.getText(), String.valueOf(d));

        button.click();
    }

    @When("^I press ([+–x\\/=])$")
    public void I_press_op(final char op) throws UiObjectNotFoundException {
        final String id;

        switch (op) {
            case '+':
                id = "btn_op_add";
                break;
            case '–':
                id = "btn_op_subtract";
                break;
            case 'x':
                id = "btn_op_multiply";
                break;
            case '/':
                id = "btn_op_divide";
                break;
            case '=':
                id = "btn_op_equals";
                break;
            default:
                id = null;
                break;
        }

        assertNotNull(id);

        UiObject button = mDevice.findObject(new UiSelector().resourceId(id(id)));
        assertEquals(button.getText(), String.valueOf(op));

        button.click();
    }

    @Then("^I should see (\\S+) on the display$")
    public void I_should_see_s_on_the_display(final String s) throws UiObjectNotFoundException {
        UiObject result = mDevice.findObject(new UiSelector().resourceId(id("txt_calc_display")));
        assertEquals(result.getText(), s);
    }
}
