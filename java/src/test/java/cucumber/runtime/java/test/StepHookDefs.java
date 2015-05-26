package cucumber.runtime.java.test;

import cucumber.api.java.AfterStep;
import cucumber.api.java.BeforeStep;
import cucumber.api.java.en.Given;
import gherkin.formatter.model.Step;

public class StepHookDefs {
    @BeforeStep
    public void beforeStep(Step step) {
        System.out.println("@BeforeStep for: " + step.getKeyword() + " " + step.getName());
    }
    
    @AfterStep
    public void afterStep(Step step) {
        System.out.println("@AfterStep for: " + step.getKeyword() + " " + step.getName());
    }
    
    @Given("^I have a pen$")
    public void pen() {
        System.out.println("--> pen");
    }
    
    @Given("^Sheet of paper$")
    public void paper() {
        System.out.println("-->paper");
    }
}