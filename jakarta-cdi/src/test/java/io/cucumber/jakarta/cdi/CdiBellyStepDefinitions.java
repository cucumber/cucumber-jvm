package io.cucumber.jakarta.cdi;

import io.cucumber.java.en.Then;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ApplicationScoped
public class CdiBellyStepDefinitions {

    @Inject
    private Belly belly;

    @Then("there are {int} cukes in my belly")
    public void checkCukes(int n) {
        assertEquals(n, belly.getCukes());
    }

}
