package integration;

import java.util.logging.Logger;

import cucumber.api.java.en.Given;
import cucumber.metrics.annotation.SpeedRegulator;
import cucumber.metrics.annotation.SpeedRegulators;
import cucumber.metrics.annotation.Timed;

public class Demo {

    private static final Logger logger = Logger.getLogger(Demo.class.getName());

    @Timed
    @SpeedRegulators({ @SpeedRegulator(application = "APP_1"), @SpeedRegulator(application = "APP_2", cost = 2000000000) })
    @Given("^me a hello, please. Best Regards '(.*)'.$")
    public void hello(String name) {
        logger.info("Hello " + name + "!");
    }
}
