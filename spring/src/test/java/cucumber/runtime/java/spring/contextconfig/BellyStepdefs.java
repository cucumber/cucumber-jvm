package cucumber.runtime.java.spring.contextconfig;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.runtime.java.spring.beans.Belly;
import cucumber.runtime.java.spring.beans.BellyBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.Assert.assertEquals;

@ContextConfiguration("classpath:cucumber.xml")
public class BellyStepdefs {

    @Autowired
    private Belly belly;

    @Autowired
    private BellyBean bellyBean;

    @Then("^there are (\\d+) cukes in my belly")
    public void checkCukes(final int n) {
        assertEquals(n, belly.getCukes());
        belly.setCukes(0);
    }

    @Given("^I have (\\d+) cukes in my belly")
    public void haveCukes(final int n) {
        belly.setCukes(n);
    }

    @Given("^I have (\\d+) beans in my belly$")
    public void I_have_beans_in_my_belly(int n) {
        bellyBean.setCukes(n);
    }

    @Then("^there are (\\d+) beans in my belly$")
    public void there_are_beans_in_my_belly(int n) {
        assertEquals(n, bellyBean.getCukes());
    }

    public BellyBean getBellyBean() {
        return bellyBean;
    }

}
