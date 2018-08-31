package io.cucumber.spring.metaconfig.general;

import cucumber.api.java.en.Then;
import io.cucumber.spring.beans.Belly;
import io.cucumber.spring.beans.BellyBean;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertNotNull;

@MetaConfiguration
public class BellyMetaStepdefs {

    @Autowired
    private Belly belly;

    @Autowired
    private BellyBean bellyBean;

    public BellyBean getBellyBean() {
        return bellyBean;
    }

    @Then("I have a meta belly")
    public void I_have_belly() throws Throwable {
        assertNotNull(belly);
    }

    @Then("I have a meta belly bean")
    public void I_have_belly_bean() throws Throwable {
        assertNotNull(bellyBean);
    }
}