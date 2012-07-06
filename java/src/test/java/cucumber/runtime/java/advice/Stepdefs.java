package cucumber.runtime.java.advice;

import cucumber.annotation.en.Given;

public class Stepdefs {
    @Timed
    @Given("^there are (\\d+) cookies$")
    public void there_are_cookies(int arg1) {
    }
}
