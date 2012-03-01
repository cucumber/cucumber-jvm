package cucumber.runtime.java.picocontainer;

import cucumber.annotation.Before;
import cucumber.annotation.en.Given;
import cucumber.annotation.en.Then;
import cucumber.annotation.en.When;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class EnumsSteps {

    private Color color;

    @Before()
    public void clearColor() {
        this.color = null;
    }

    @Given("^I want to recognize colors as enums$")
    public void I_want_to_recognize_colors_as_enums() {
    }

    @When("^i use the (.*) in a step$")
    public void i_use_the_color_in_a_step(Color color) {
    }

    @Then("^it should be recognized as enum$")
    public void it_should_be_recognized_as_enum() {
    }


    public static class ColorRecognized {
        public Color color;
        public boolean recognized;
    }

    public static enum Color {
        RED, BLUE, GREEN
    }
}
