package cucumber.runtime.java8;

import static org.junit.Assert.assertEquals;

import cucumber.api.java8.StepdefBody;
import cucumber.runtime.CucumberException;
import org.junit.Test;

import java.util.List;

public class Java8LambdaStepDefinitionTest {

    @Test
    public void should_calculate_parameters_count_from_body_with_one_param() throws Exception {
        StepdefBody.A1<String> body = p1 -> {
        };
        Java8StepDefinition def = new Java8StepDefinition("^I have (\\d) some step (.*)$", 0, StepdefBody.A1.class, body);
        assertEquals(Integer.valueOf(1), def.getParameterCount());
    }

    @Test
    public void should_calculate_parameters_count_from_body_with_two_params() throws Exception {
        StepdefBody.A2<String, String> body = (p1, p2) -> {
        };
        Java8StepDefinition def = new Java8StepDefinition("^I have some step $", 0, StepdefBody.A2.class, body);
        assertEquals(Integer.valueOf(2), def.getParameterCount());
    }

    @Test
    public void should_fail_for_param_with_non_generic_list() throws Exception {
        try {
            StepdefBody.A1<List> body = p1 -> {
            };
            new Java8StepDefinition("^I have (\\d) some step (.*)$", 0, StepdefBody.A1.class, body);
        } catch (CucumberException expected) {
            assertEquals("Can't use java.util.List in lambda step definition. Declare a DataTable argument instead and convert manually with asList/asLists/asMap/asMaps", expected.getMessage());
        }
    }

    @Test
    public void should_fail_for_param_with_generic_list() throws Exception {
        try {
            StepdefBody.A1<List<String>> body = p1 -> {
            };
            new Java8StepDefinition("^I have (\\d) some step (.*)$", 0, StepdefBody.A1.class, body);
        } catch (CucumberException expected) {
            assertEquals("Can't use java.util.List in lambda step definition. Declare a DataTable argument instead and convert manually with asList/asLists/asMap/asMaps", expected.getMessage());
        }
    }
}
