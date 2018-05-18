package cucumber.runtime.java.guice.integration;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.ArrayList;
import java.util.List;

import static cucumber.runtime.java.guice.matcher.ElementsAreAllUniqueMatcher.elementsAreAllUnique;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

public class UnScopedSteps {

    private static final List<UnScopedObject> OBJECTS = new ArrayList<UnScopedObject>(3);
    private final Provider<UnScopedObject> unScopedObjectProvider;

    @Inject
    public UnScopedSteps(Provider<UnScopedObject> unScopedObjectProvider) {
        this.unScopedObjectProvider = unScopedObjectProvider;
    }

    @Given("an un-scoped instance has been provided in this scenario")
    public void an_un_scoped_instance_has_been_provided_in_this_scenario() throws Throwable {
        OBJECTS.clear();
        provide();
    }

    @When("another un-scoped instance is provided")
    public void another_un_scoped_instance_is_provided() throws Throwable {
        provide();
    }

    @Then("all three provided instances are unique instances")
    public void all_three_provided_instances_are_unique_instances() throws Throwable {
        assertThat("Expected test scenario to provide three objects.", OBJECTS.size(), equalTo(3));
        assertThat(OBJECTS, elementsAreAllUnique());
    }

    private void provide() {
        UnScopedObject unScopedObject = unScopedObjectProvider.get();
        assertThat(unScopedObject, notNullValue());
        OBJECTS.add(unScopedObject);
    }
}
