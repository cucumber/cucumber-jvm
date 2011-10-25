package cucumber.runtime.java.guice.loadguicemodule;

import cucumber.annotation.en.And;
import cucumber.annotation.en.When;

import javax.inject.Inject;

public class FirstSteps {

    private final SharedBetweenSteps shared;

    @Inject
    public FirstSteps(SharedBetweenSteps shared) {
        this.shared = shared;
    }

    @And("^the class SharedBetweenSteps is bound to a single instance$")
    public void the_class_SharedBetweenSteps_is_bound_to_a_single_instance() {
        //have a look at the module class
    }

    @When("^the first step class visits the instance of SharedBetweenSteps$")
    public void the_first_step_class_visits_the_instance_of_SharedBetweenSteps() {
        shared.visit();
    }
}