package io.cucumber.guice.integration;

import io.cucumber.guice.ScenarioScoped;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import javax.inject.Inject;
import javax.inject.Provider;

import java.util.ArrayList;
import java.util.List;

import static io.cucumber.guice.collection.CollectionUtil.removeAllExceptFirstElement;
import static io.cucumber.guice.matcher.ElementsAreAllEqualMatcher.elementsAreAllEqual;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@ScenarioScoped
public class SingletonScopedSteps {

    private static final List<SingletonObject> OBJECTS = new ArrayList<>(3);
    private final Provider<SingletonObject> singletonObjectProvider;

    @Inject
    public SingletonScopedSteps(Provider<SingletonObject> singletonObjectProvider) {
        this.singletonObjectProvider = singletonObjectProvider;
    }

    @Given("a singleton scope instance has been provided in this scenario")
    public void a_singleton_scope_instance_has_been_provided_in_this_scenario() {
        OBJECTS.clear();
        provide();
    }

    private void provide() {
        SingletonObject singletonObject = singletonObjectProvider.get();
        assertThat(singletonObject, notNullValue());
        OBJECTS.add(singletonObject);
    }

    @When("another singleton scope instance is provided")
    public void another_singleton_scope_instance_is_provided() {
        provide();
    }

    @Then("all three provided instances are the same singleton instance")
    public void all_three_provided_instances_are_the_same_singleton_instance() {
        assertThat("Expected test scenario to provide three objects.", OBJECTS.size(), equalTo(3));
        assertThat(OBJECTS, elementsAreAllEqual());
    }

    @Given("a singleton scope instance was provided in the previous scenario")
    public void a_singleton_scope_instance_was_provided_in_the_previous_scenario() {
        // we only need one instance from the previous scenario
        removeAllExceptFirstElement(OBJECTS);
    }

    @Then("the two provided instances are the same instance")
    public void the_two_provided_instances_are_the_same_instance() {
        assertThat("Expected test scenario to provide two objects.", OBJECTS.size(), equalTo(2));
        assertThat(OBJECTS, elementsAreAllEqual());
    }

}
