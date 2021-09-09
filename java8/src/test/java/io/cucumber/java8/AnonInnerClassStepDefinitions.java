package io.cucumber.java8;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AnonInnerClassStepDefinitions implements LambdaGlue {

    @SuppressWarnings("Convert2Lambda")
    public AnonInnerClassStepDefinitions() {
        LambdaGlueRegistry.INSTANCE.get().addStepDefinition(
            Java8StepDefinition.create(
                "I have {int} java7 beans in my {word}", StepDefinitionBody.A2.class,
                new StepDefinitionBody.A2<Integer, String>() {
                    @Override
                    public void accept(Integer cukes, String what) {
                        assertEquals(42, cukes.intValue());
                        assertEquals("belly", what);
                    }
                }));
    }

}
