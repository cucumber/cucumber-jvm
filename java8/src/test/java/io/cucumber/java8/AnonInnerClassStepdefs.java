package io.cucumber.java8;

import io.cucumber.core.backend.StepDefinition;
import io.cucumber.core.stepexpression.TypeRegistry;

import java.util.function.Function;

import static org.junit.Assert.assertEquals;

public class AnonInnerClassStepdefs implements LambdaGlue {

    public AnonInnerClassStepdefs() {
        Java8Backend.INSTANCE.get().addStepDefinition(new Function<TypeRegistry, StepDefinition>() {
            @Override
            public StepDefinition apply(TypeRegistry typeRegistry) {
                return Java8StepDefinition.create(
                    "I have {int} java7 beans in my {word}", StepdefBody.A2.class,
                    new StepdefBody.A2<Integer, String>() {
                        @Override
                        public void accept(Integer cukes, String what) throws Throwable {
                            assertEquals(42, cukes.intValue());
                            assertEquals("belly", what);
                        }
                    }, typeRegistry);
            }
        });
    }
}
