package cucumber.examples.java.interop;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

public class InteropStepDefs {

    @Given("a publisher & subscriber with compatible endpoint configurations")
    public void pubSubCompatibleConfigs() {

    }

    @Given("a Java subscriber")
    public void aJavaSubscriber() {
        System.out.println("A Java subscriber");
    }

    @And("a Java publisher")
    public void aJavaPublisher() {
        System.out.println("A Java publisher");
    }

    @When("the Java publisher publishes (.*)")
    public void theJavaPublisherPublishes(String data) {
        System.out.println("The Java publisher publishes " + data);
    }

    @Then("the Java subscriber receives (.*)")
    public void theJavaSubscriberReceives(String data) {
        System.out.println("The Java subscriber receives " + data);
    }
}
