package io.cucumber.spring.deprecatedglue;

import io.cucumber.java.en.Given;
import io.cucumber.spring.beans.BellyBean;

public class StepDefsWithConstructorArgs {

    private final BellyBean bellyBean;

    public StepDefsWithConstructorArgs(BellyBean bellyBean) {
        this.bellyBean = bellyBean;
    }

    @Given("a belly bean via constructor dependency injection")
    public void the_StepDef_injection_works() {

    }
}
