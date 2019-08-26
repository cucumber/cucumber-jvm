package cucumber.runtime.java.java8test;

import cucumber.api.java8.GlueBase;
import cucumber.api.java8.StepdefBody;
import cucumber.runtime.java.JavaBackend;

import static org.junit.Assert.assertEquals;

public class AnonInnerClassStepdefs implements GlueBase {

    public AnonInnerClassStepdefs() {
        JavaBackend.INSTANCE.get().addStepDefinition("^I have (\\d+) cukes in my (.*)", 0, new StepdefBody.A2<Integer, String>() {
            @Override
            public void accept(Integer cukes, String what) {
                assertEquals(42, cukes.intValue());
                assertEquals("belly", what);
            }
        }, null);
    }
}
