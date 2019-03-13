package cucumber.runtime.java.spring.contextcaching;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@ContextConfiguration(classes = {ContextConfig.class})
public class ContextCachingSteps {

    @Autowired
    ContextCounter contextCounter;

    @cucumber.api.java.en.When("I run a scenario")
    public void runningScenario() {
    }

    @cucumber.api.java.en.Then("there should be only one Spring context")
    public void oneContext() {
        assertThat(contextCounter.getContextCount(), is(1));
    }

}
