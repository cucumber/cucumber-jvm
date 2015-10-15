package cucumber.runtime.java.weld.jmockit;

import java.util.ArrayList;
import java.util.List;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;

import javax.inject.Singleton;

import mockit.Mocked;
import mockit.StrictExpectations;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Singleton
public class JMockitStepdefs {

	final List<Integer> capturedCukes = new ArrayList<Integer>();
	
    @Mocked
    private Belly belly;

    private boolean inTheBelly = false;

    @Given("^I have (\\d+) mocks in my belly")
    public void haveCukes(int n) {
    	new StrictExpectations() {{
    		belly.setCukes(withCapture(capturedCukes));
    		times = 1;
    	}};
        belly.setCukes(n);
        inTheBelly = true;
    }

    @Then("^there are (\\d+) mocks in my belly")
    public void checkCukes(int n) {
    	new StrictExpectations() {{
    		belly.getCukes();
    		times = 1;
    		result = capturedCukes.get(0);
    	}};
        assertEquals(n, belly.getCukes());
        assertTrue(inTheBelly);
    }
}
