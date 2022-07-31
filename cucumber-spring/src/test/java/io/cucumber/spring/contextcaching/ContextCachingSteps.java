package io.cucumber.spring.contextcaching;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@CucumberContextConfiguration
@ContextConfiguration(classes = ContextConfig.class)
public class ContextCachingSteps {

    @Autowired
    ContextCounter contextCounter;

    @When("I run a scenario in the same JVM as the SharedContextTest")
    public void runningScenario() {
        // happens automatically
    }

    @Then("there should be only one Spring context")
    public void oneContext() {
        assertThat(contextCounter.getContextCount(), is(1));
    }

}
