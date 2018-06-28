package cucumber.runtime.java8;

import static org.junit.Assert.assertEquals;

import io.cucumber.stepexpression.TypeRegistry;
import cucumber.api.java8.StepdefBody;
import cucumber.runtime.CucumberException;
import org.junit.Test;

import java.util.List;
import java.util.Locale;

public class Java8LambdaStepDefinitionTest {

    private final TypeRegistry typeRegistry = new TypeRegistry(Locale.ENGLISH);

    @Test
    public void should_calculate_parameters_count_from_body_with_one_param() {
        StepdefBody.A1<String> body = p1 -> {
        };
        Java8StepDefinition def = Java8StepDefinition.create("I have some step", StepdefBody.A1.class, body, typeRegistry);
        assertEquals(Integer.valueOf(1), def.getParameterCount());
    }

    @Test
    public void should_calculate_parameters_count_from_body_with_two_params() {
        StepdefBody.A2<String, String> body = (p1, p2) -> {
        };
        Java8StepDefinition def = Java8StepDefinition.create("I have some step", StepdefBody.A2.class, body, typeRegistry);
        assertEquals(Integer.valueOf(2), def.getParameterCount());
    }

    @Test
    public void should_fail_for_param_with_non_generic_list() {
        try {
            StepdefBody.A1<List> body = p1 -> {
            };
            Java8StepDefinition.create("I have some step", StepdefBody.A1.class, body, typeRegistry);
        } catch (CucumberException expected) {
            assertEquals("Can't use java.util.List in lambda step definition. Declare a DataTable argument instead and convert manually with asList/asLists/asMap/asMaps", expected.getMessage());
        }
    }

    @Test
    public void should_fail_for_param_with_generic_list() {
        try {
            StepdefBody.A1<List<String>> body = p1 -> {
            };
            Java8StepDefinition.create("I have some step", StepdefBody.A1.class, body, typeRegistry);
        } catch (CucumberException expected) {
            assertEquals("Can't use java.util.List in lambda step definition. Declare a DataTable argument instead and convert manually with asList/asLists/asMap/asMaps", expected.getMessage());
        }
    }
}
