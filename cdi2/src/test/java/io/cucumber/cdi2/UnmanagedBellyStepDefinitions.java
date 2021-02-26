package io.cucumber.cdi2;

import io.cucumber.java.en.Given;

import javax.inject.Inject;

public class UnmanagedBellyStepDefinitions {

    @Inject
    private Belly belly;

    @Given("I have {int} unmanaged cukes in my belly")
    public void haveCukes(int n) {
        belly.setCukes(n);
    }

    @Given("I eat {int} more cukes")
    public void addCukes(int n) {
        belly.setCukes(belly.getCukes() + n);
    }

}
