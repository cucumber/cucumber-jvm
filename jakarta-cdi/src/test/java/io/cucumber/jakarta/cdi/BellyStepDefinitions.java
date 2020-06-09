package io.cucumber.jakarta.cdi;

import io.cucumber.java.en.Given;
import jakarta.enterprise.inject.Vetoed;
import jakarta.inject.Inject;

@Vetoed
public class BellyStepDefinitions {

    @Inject
    private Belly belly;

    @Given("I have {int} cukes in my belly")
    public void haveCukes(int n) {
        belly.setCukes(n);
    }

}
