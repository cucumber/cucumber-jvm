package io.cucumber.spring.contextcaching;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.hamcrest.MatcherAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@ContextConfiguration(classes = {ContextConfig.class})
public class ContextCachingSteps {

    @Autowired
    ContextCounter contextCounter;

    @When("I run a scenario")
    public void runningScenario() {
    }

    @Then("there should be only one Spring context")
    public void oneContext() {
        assertThat(contextCounter.getContextCount(), is(1));
    }

}
