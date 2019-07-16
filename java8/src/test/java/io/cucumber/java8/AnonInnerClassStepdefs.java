package io.cucumber.java8;

import static org.junit.Assert.assertEquals;

public class AnonInnerClassStepdefs implements LambdaGlue {

    public AnonInnerClassStepdefs() {
        LambdaGlueRegistry.INSTANCE.get().addStepDefinition(
            Java8StepDefinition.create(
                "I have {int} java7 beans in my {word}", StepdefBody.A2.class,
                new StepdefBody.A2<Integer, String>() {
                    @Override
                    public void accept(Integer cukes, String what) throws Throwable {
                        assertEquals(42, cukes.intValue());
                        assertEquals("belly", what);
                    }
                }));
    }
}
