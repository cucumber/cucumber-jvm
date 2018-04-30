package cucumber.runtime.java8;

import static org.junit.Assert.assertEquals;

import io.cucumber.stepexpression.TypeRegistry;
import cucumber.api.java8.StepdefBody;
import org.junit.Test;

import java.util.List;
import java.util.Locale;

public class Java8AnonInnerClassStepDefinitionTest {

    private final TypeRegistry typeRegistry = new TypeRegistry(Locale.ENGLISH);

    @Test
    public void should_calculate_parameters_count_from_body_with_one_param() {
        Java8StepDefinition java8StepDefinition = Java8StepDefinition.create("I have some step", StepdefBody.A1.class, oneParamStep(), typeRegistry);
        assertEquals(Integer.valueOf(1), java8StepDefinition.getParameterCount());
    }

    @Test
    public void should_calculate_parameters_count_from_body_with_two_params() {
        Java8StepDefinition java8StepDefinition = Java8StepDefinition.create("I have some step", StepdefBody.A2.class, twoParamStep(), typeRegistry);
        assertEquals(Integer.valueOf(2), java8StepDefinition.getParameterCount());
    }

    private StepdefBody.A1 oneParamStep() {
        return new StepdefBody.A1<String>() {
            @Override
            public void accept(String p1) {
            }
        };
    }

    private StepdefBody.A2 twoParamStep() {
        return new StepdefBody.A2<String, String>() {
            @Override
            public void accept(String p1, String p2) {
            }
        };
    }

    private StepdefBody.A1 genericListStep() {
        return new StepdefBody.A1<List<String>>() {
            @Override
            public void accept(List<String> p1) {
                throw new UnsupportedOperationException();
            }
        };
    }

    private StepdefBody.A1 nonGenericListStep() {
        return new StepdefBody.A1<List>() {
            @Override
            public void accept(List p1) {
                throw new UnsupportedOperationException();
            }
        };
    }
}
