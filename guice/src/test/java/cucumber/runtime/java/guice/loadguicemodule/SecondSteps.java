package cucumber.runtime.java.guice.loadguicemodule;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import javax.inject.Inject;


import cucumber.annotation.en.Then;

public class SecondSteps {
    
    private final SharedBetweenSteps shared;

    @Inject
    public SecondSteps(SharedBetweenSteps shared) {
        this.shared = shared;
    }

    @Then("^the instance passed to the second step class is still visited$")
    public void the_instance_passed_to_the_second_step_class_is_still_visited() {
        assertThat(shared.visited, is(true));
    }
}