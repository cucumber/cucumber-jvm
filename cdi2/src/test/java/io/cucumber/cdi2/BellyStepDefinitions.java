package io.cucumber.cdi2;

import io.cucumber.java.en.Given;

import javax.enterprise.inject.Vetoed;
import javax.inject.Inject;

@Vetoed
public class BellyStepDefinitions {

    @Inject
    private Belly belly;

    @Given("I have {int} cukes in my belly")
    public void haveCukes(int n) {
        belly.setCukes(n);
    }

}
