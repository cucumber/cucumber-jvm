package cucumber.runtime.java.spring.dirtiescontextconfig;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.runtime.java.spring.beans.Belly;
import cucumber.runtime.java.spring.beans.BellyBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.Assert.assertEquals;

@ContextConfiguration("classpath:cucumber.xml")
@DirtiesContext
public class DirtiesContextBellyStepDefs {

    @Autowired
    private Belly belly;

    @Autowired
    private BellyBean bellyBean;

    @Then("there are {int} dirty cukes in my belly")
    public void checkCukes(final int n) {
        assertEquals(n, belly.getCukes());
    }

    @Given("I have {int} dirty cukes in my belly")
    public void haveCukes(final int n) {
        assertEquals(0, belly.getCukes());
        belly.setCukes(n);
    }

    @Given("I have {int} dirty beans in my belly")
    public void I_have_beans_in_my_belly(int n) {
        assertEquals(0, bellyBean.getCukes());
        bellyBean.setCukes(n);
    }

    @Then("there are {int} dirty beans in my belly")
    public void there_are_beans_in_my_belly(int n) {
        assertEquals(n, bellyBean.getCukes());
    }

    public BellyBean getBellyBean() {
        return bellyBean;
    }

}
