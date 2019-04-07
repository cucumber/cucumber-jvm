package cucumber.runtime.java.spring.commonglue;

import cucumber.api.java.en.Given;
import cucumber.runtime.java.spring.beans.BellyBean;

public class StepDefsWithConstructorArgs {

    private final BellyBean bellyBean;

    public StepDefsWithConstructorArgs(BellyBean bellyBean) {
        this.bellyBean = bellyBean;
    }

    @Given("a belly bean via constructor dependency injection")
    public void the_StepDef_injection_works() {

    }
}
