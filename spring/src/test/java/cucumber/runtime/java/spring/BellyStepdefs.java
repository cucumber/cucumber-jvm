package cucumber.runtime.java.spring;

import cucumber.api.java.en.Then;
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

    @Then("^I have belly$")
    public void I_have_belly() throws Throwable {
        assertNotNull(belly);
    }

    @Then("^I have belly bean$")
    public void I_have_belly_bean() throws Throwable {
        assertNotNull(bellyBean);
    }
}
