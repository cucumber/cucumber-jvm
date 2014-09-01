package cucumber.runtime.java.java8test;

import cucumber.api.java8.Language;
import cucumber.api.java8.StepdefBody;
import cucumber.runtime.java.JavaBackend;

import static org.junit.Assert.assertEquals;

public class AnonInnerClassStepdefs implements Language {

    public void defineGlue() {
        JavaBackend.INSTANCE.get().addStepDefinition("^I have (\\d+) cukes in my (.*)", 0, new StepdefBody.A2() {
            public void accept(String cukes, String what) {
                assertEquals("42", cukes);
                assertEquals("belly", what);
            }
        });
    }
}
