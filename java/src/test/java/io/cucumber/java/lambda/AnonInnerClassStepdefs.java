package io.cucumber.java.lambda;

import static org.junit.Assert.assertEquals;

import io.cucumber.core.stepexpression.TypeRegistry;
import io.cucumber.java.Java8StepDefinition;
import io.cucumber.java.JavaBackend;
import io.cucumber.java.LambdaGlue;
import io.cucumber.java.api.StepdefBody;
import io.cucumber.core.backend.StepDefinition;

import java.util.function.Function;

public class AnonInnerClassStepdefs implements LambdaGlue {

    public AnonInnerClassStepdefs() {
        JavaBackend.INSTANCE.get().addStepDefinition(new Function<TypeRegistry, StepDefinition>() {
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
