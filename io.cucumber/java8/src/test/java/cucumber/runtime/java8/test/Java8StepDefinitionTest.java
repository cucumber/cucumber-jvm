package cucumber.runtime.java8.test;

import cucumber.api.java8.StepdefBody;
import cucumber.runtime.CucumberException;
import cucumber.runtime.java.Java8StepDefinition;
import cucumber.runtime.java.TypeIntrospector;
import cucumber.runtime.java8.ConstantPoolTypeIntrospector;
import org.junit.Test;

import java.util.List;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;

public class Java8StepDefinitionTest {
    private final TypeIntrospector typeIntrospector = new ConstantPoolTypeIntrospector();

    @Test
    public void should_calculate_parameters_count_from_body_with_one_param() throws Exception {
        StepdefBody body = (StepdefBody.A1<String>) p1 -> {
        };
        Java8StepDefinition def = new Java8StepDefinition(Pattern.compile("^I have (\\d) some step (.*)$"), 0, body, typeIntrospector);
        assertEquals(new Integer(1), def.getParameterCount());
    }

    @Test
    public void should_calculate_parameters_count_from_body_with_two_params() throws Exception {
        StepdefBody body = (StepdefBody.A2<String, String>) (p1, p2) -> {
        };
        Java8StepDefinition def = new Java8StepDefinition(Pattern.compile("^I have some step $"), 0, body, typeIntrospector);
        assertEquals(new Integer(2), def.getParameterCount());
    }

    @Test
    public void should_fail_for_param_with_non_generic_list() throws Exception {
        try {
            StepdefBody body = (StepdefBody.A1<List>) p1 -> {
            };
            new Java8StepDefinition(Pattern.compile("^I have (\\d) some step (.*)$"), 0, body, typeIntrospector);
        } catch (CucumberException expected) {
            assertEquals("Can't use java.util.List in lambda step definition. Declare a DataTable argument instead and convert manually with asList/asLists/asMap/asMaps", expected.getMessage());
        }
    }

    @Test
    public void should_pass_for_param_with_generic_list() throws Exception {
        try {
            StepdefBody body = (StepdefBody.A1<List<String>>) p1 -> {
            };
            new Java8StepDefinition(Pattern.compile("^I have (\\d) some step (.*)$"), 0, body, typeIntrospector);
        } catch (CucumberException expected) {
            assertEquals("Can't use java.util.List in lambda step definition. Declare a DataTable argument instead and convert manually with asList/asLists/asMap/asMaps", expected.getMessage());
        }
    }

}
