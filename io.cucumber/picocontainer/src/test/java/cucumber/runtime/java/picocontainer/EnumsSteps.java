package cucumber.runtime.java.picocontainer;

import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

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
