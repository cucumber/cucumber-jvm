package cucumber.runtime.java.spring.metaconfig.general;

import cucumber.api.java.en.Then;
import cucumber.runtime.java.spring.beans.Belly;
import cucumber.runtime.java.spring.beans.BellyBean;
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