package io.cucumber.spring.metaconfig.general;

import io.cucumber.java.en.Then;
import io.cucumber.spring.CucumberContextConfiguration;
import io.cucumber.spring.beans.Belly;
import io.cucumber.spring.beans.BellyBean;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@CucumberContextConfiguration
@MetaConfiguration
public class BellyMetaStepDefinitions {

    @Autowired
    private Belly belly;

    @Autowired
    private BellyBean bellyBean;

    public BellyBean getBellyBean() {
        return bellyBean;
    }

    @Then("I have a meta belly")
    public void I_have_belly() {
        assertNotNull(belly);
    }

    @Then("I have a meta belly bean")
    public void I_have_belly_bean() {
        assertNotNull(bellyBean);
    }

}
