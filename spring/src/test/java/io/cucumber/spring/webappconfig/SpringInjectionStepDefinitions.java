package io.cucumber.spring.webappconfig;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@CucumberContextConfiguration
@WebAppConfiguration
@ContextConfiguration("classpath:cucumber.xml")
public class SpringInjectionStepDefinitions {

    @Autowired
    private WebApplicationContext wac;

    private ResultActions callUrl;

    @Given("I have the web context set")
    public void I_have_the_web_context_set() {
        assertNotNull(wac);
    }

    @When("I call the url {string}")
    public void I_call_the_url(String url) throws Throwable {
        MockMvc mock = MockMvcBuilders.webAppContextSetup(wac).build();
        callUrl = mock.perform(get(url));
    }

    @Then("it should return {int}")
    public void it_should_return(int httpCode) throws Throwable {
        callUrl.andExpect(status().is(httpCode));
    }

}
