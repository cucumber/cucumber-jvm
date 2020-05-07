package io.cucumber.guice.integration;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import javax.inject.Inject;
import javax.inject.Provider;

import java.util.ArrayList;
import java.util.List;

import static io.cucumber.guice.matcher.ElementsAreAllUniqueMatcher.elementsAreAllUnique;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class UnScopedSteps {

    private static final List<UnScopedObject> OBJECTS = new ArrayList<>(3);
    private final Provider<UnScopedObject> unScopedObjectProvider;

    @Inject
    public UnScopedSteps(Provider<UnScopedObject> unScopedObjectProvider) {
        this.unScopedObjectProvider = unScopedObjectProvider;
    }

    @Given("an un-scoped instance has been provided in this scenario")
    public void an_un_scoped_instance_has_been_provided_in_this_scenario() {
        OBJECTS.clear();
        provide();
    }

    private void provide() {
        UnScopedObject unScopedObject = unScopedObjectProvider.get();
        assertThat(unScopedObject, notNullValue());
        OBJECTS.add(unScopedObject);
    }

    @When("another un-scoped instance is provided")
    public void another_un_scoped_instance_is_provided() {
        provide();
    }

    @Then("all three provided instances are unique instances")
    public void all_three_provided_instances_are_unique_instances() {
        assertThat("Expected test scenario to provide three objects.", OBJECTS.size(), equalTo(3));
        assertThat(OBJECTS, elementsAreAllUnique());
    }

}
