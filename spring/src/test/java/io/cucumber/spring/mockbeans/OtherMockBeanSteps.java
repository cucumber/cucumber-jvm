package cucumber.runtime.java.spring.mockbeans;

import cucumber.api.java.en.Then;
import cucumber.runtime.java.spring.beans.Belly;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.junit.Assert.assertEquals;

public class OtherMockBeanSteps {

    @MockBean
    private Belly belly;

    @Then("the belly contains {int} cukes")
    public void checkCukes(int cukes) {
        assertEquals(cukes, belly.getCukes());
    }


}
