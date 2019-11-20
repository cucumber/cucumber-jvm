package io.cucumber.spring.mockbeans;

import org.springframework.boot.test.mock.mockito.MockBean;

import io.cucumber.java.en.Then;
import io.cucumber.spring.beans.Belly;

import static org.junit.Assert.assertEquals;

public class OtherMockBeanSteps {

    @MockBean
    private Belly belly;

    @Then("the belly contains {int} cukes")
    public void checkCukes(int cukes) {
        assertEquals(cukes, belly.getCukes());
    }


}
