package io.cucumber.spring.mockbeans;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;


import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.spring.beans.Belly;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.when;

@ContextConfiguration("classpath:cucumber.xml")
@DirtiesContext
public class MockBeanSteps {

    @MockBean
    private Belly belly;

    @Given("a mocked belly is not a real belly")
    public void checkBelly() {
        assertNotEquals(Belly.class, belly.getClass());
        assertEquals(0, belly.getCukes());
    }

    @When("the belly is mocked to contain {int} cukes")
    public void mockCukes(int cukes) {
        when(belly.getCukes()).thenReturn(cukes);
    }

}
