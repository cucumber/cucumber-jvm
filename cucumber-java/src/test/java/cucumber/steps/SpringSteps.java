package cucumber.steps;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cucumber.Given;
import cucumber.Then;
import cucumber.spring.SpringService;

@Component
public class SpringSteps {
	private final Map<String,Integer> cukes = new HashMap<String,Integer>();
	
	@Autowired
	private SpringService service;
	
	@Given("I have (\\d+) (.*) cukes")
    public void iHaveNCukes(int n, String color) {
        this.cukes.put(color, n);
    }
	
	@Given("I say hello")
	public void iSayHello() {
		String hello = service.hello();
		if (!hello.equals("Have a cuke, Duke")) {
			throw new RuntimeException("Wrong reply from service");
		}
	}
	
	@Then("I should have (\\d+) (.*) cukes")
    public void iShouldHaveNCukes(int n, String color) {
        if(n != cukes.get(color)) {
            throw new RuntimeException("Expected " + n + ", got " + cukes.get(color));
        }
    }
}
