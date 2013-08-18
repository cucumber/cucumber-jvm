package cucumber.runtime.java.spring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;

import static org.junit.Assert.assertEquals;

@ContextConfiguration("classpath:cucumber.xml")
@DirtiesContext
public class DirtiesContextBellyStepdefs {

    @Autowired
    private Belly belly;

    @Autowired
    private BellyBean bellyBean;

    @Then("^there are (\\d+) dirty cukes in my belly")
    public void checkCukes(final int n) {
        assertEquals(n, belly.getCukes());
    }

    @Given("^I have (\\d+) dirty cukes in my belly")
    public void haveCukes(final int n) {
        belly.setCukes(n);
    }

    @Given("^I have (\\d+) dirty beans in my belly$")
    public void I_have_beans_in_my_belly(int n) {
        bellyBean.setCukes(n);
    }

    @Then("^there are (\\d+) dirty beans in my belly$")
    public void there_are_beans_in_my_belly(int n) {
        assertEquals(n, bellyBean.getCukes());
    }

    public BellyBean getBellyBean() {
        return bellyBean;
    }

}
