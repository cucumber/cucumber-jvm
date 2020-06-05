package io.cucumber.spring.contextconfig;

import io.cucumber.java.en.Then;
import io.cucumber.spring.CucumberContextConfiguration;
import io.cucumber.spring.beans.Belly;
import io.cucumber.spring.beans.BellyBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@CucumberContextConfiguration
@ContextConfiguration("classpath:cucumber.xml")
public class BellyStepDefinitions {

    @Autowired
    private Belly belly;

    @Autowired
    private BellyBean bellyBean;

    public BellyBean getBellyBean() {
        return bellyBean;
    }

    @Then("I have belly")
    public void I_have_belly() {
        assertNotNull(belly);
    }

    @Then("I have belly bean")
    public void I_have_belly_bean() {
        assertNotNull(bellyBean);
    }

}
