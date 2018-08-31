package io.cucumber.spring.contextconfig;

import cucumber.api.java.en.Then;
import io.cucumber.spring.beans.Belly;
import io.cucumber.spring.beans.BellyBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.Assert.assertNotNull;

@ContextConfiguration("classpath:cucumber.xml")
public class BellyStepdefs {

    @Autowired
    private Belly belly;

    @Autowired
    private BellyBean bellyBean;

    public BellyBean getBellyBean() {
        return bellyBean;
    }

    @Then("I have belly")
    public void I_have_belly() throws Throwable {
        assertNotNull(belly);
    }

    @Then("I have belly bean")
    public void I_have_belly_bean() throws Throwable {
        assertNotNull(bellyBean);
    }
}