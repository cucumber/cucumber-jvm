package integration;

import java.util.logging.Logger;

import cucumber.api.java.en.Given;
import cucumber.metric.annotation.SpeedRegulator;
import cucumber.metric.annotation.SpeedRegulators;
import cucumber.metric.annotation.Timed;

public class Demo {

    private static final Logger logger = Logger.getLogger(Demo.class.getName());

    @Timed
    @SpeedRegulators({ @SpeedRegulator(application = "SALTO"), @SpeedRegulator(application = "ACASIA", cost = 2000000000) })
    @Given("^me a hello, please. Best Regards '(.*)'.$")
    public void hello(String name) throws InterruptedException, InstantiationException, IllegalAccessException {
        logger.info("Hello " + name + "!");
    }
}
