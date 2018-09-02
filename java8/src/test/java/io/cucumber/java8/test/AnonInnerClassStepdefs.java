package io.cucumber.java8.test;

import static org.junit.Assert.assertEquals;

import io.cucumber.core.stepexpression.TypeRegistry;
import cucumber.api.java8.GlueBase;
import cucumber.api.java8.StepdefBody;
import io.cucumber.core.backend.StepDefinition;
import io.cucumber.java.Function;
import io.cucumber.java.JavaBackend;
import io.cucumber.java8.Java8StepDefinition;

public class AnonInnerClassStepdefs implements GlueBase {

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
