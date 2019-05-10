package cucumber.runtime.java.cdi2;

import cucumber.api.java.en.Given;

import javax.enterprise.inject.Vetoed;
import javax.inject.Inject;

@Vetoed
public class BellyStepdefs {

    @Inject
    private Belly belly;

    @Given("I have {int} cukes in my belly")
    public void haveCukes(int n) {
        belly.setCukes(n);
    }
}
