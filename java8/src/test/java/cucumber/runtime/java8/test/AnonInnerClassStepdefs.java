package cucumber.runtime.java8.test;

import static org.junit.Assert.assertEquals;

import cucumber.api.java8.GlueBase;
import cucumber.api.java8.StepdefBody;
import cucumber.runtime.java.JavaBackend;
import cucumber.runtime.java8.Java8StepDefinition;

public class AnonInnerClassStepdefs implements GlueBase {

    public AnonInnerClassStepdefs() {
        JavaBackend.INSTANCE.get().addStepDefinition(
            new Java8StepDefinition(
                "^I have (\\d+) java7 beans in my (.*)", 0, StepdefBody.A2.class,
                new StepdefBody.A2<Integer, String>() {
                    @Override
                    public void accept(Integer cukes, String what) throws Throwable {
                        assertEquals(42, cukes.intValue());
                        assertEquals("belly", what);
                    }
                }));
    }
}
