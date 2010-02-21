package cuke4duke.steps;

import cuke4duke.annotation.English.*;
import cuke4duke.app.HelloService;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractSteps {
    private final Map<String, Integer> cukes = new HashMap<String, Integer>();

    protected abstract HelloService getHelloService();

    @Given("I have (\\d+) (.*) cukes")
    public void iHaveNCukes(int n, String color) {
        this.cukes.put(color, n);
    }

    @Then("I should have (\\d+) (.*) cukes")
    public void iShouldHaveNCukes(int n, String color) {
        if (n != cukes.get(color)) {
            throw new RuntimeException("Expected " + n + ", got " + cukes.get(color));
        }
    }

    @Given("Longs: (\\d+)")
    public void longs(long n) {
    }

    @Given("I say hello")
    public void iSayHello() {
        String hello = getHelloService().hello();
        if (!hello.equals("Have a cuke, Duke")) {
            throw new RuntimeException("Wrong reply from service");
        }
    }

    public void thisIsNotAStep() {
    }
}
