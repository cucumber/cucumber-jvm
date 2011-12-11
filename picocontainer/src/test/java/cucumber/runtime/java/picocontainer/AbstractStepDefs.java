package cucumber.runtime.java.picocontainer;

import cucumber.annotation.en.Given;

public abstract class AbstractStepDefs {

    @SuppressWarnings("unused")
    private int amount;

    @Given(value = "^I have (\\d+) (.*) in my belly$")
    public void I_have_n_things_in_my_belly(final int amount,
            @SuppressWarnings("unused") final String what) {
        this.amount = amount;
    }

}
