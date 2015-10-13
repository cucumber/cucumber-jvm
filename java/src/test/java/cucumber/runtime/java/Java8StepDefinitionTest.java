package cucumber.runtime.java;

import java.util.List;
import java.util.regex.Pattern;

import cucumber.runtime.CucumberException;
import org.junit.Assert;
import org.junit.Test;

import cucumber.api.java8.StepdefBody;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class Java8StepDefinitionTest {

    private Java8StepDefinition java8StepDefinition;

    @Test
    public void should_calculate_parameters_count_from_body_with_one_param() throws Exception {
        java8StepDefinition = new Java8StepDefinition(Pattern.compile("^I have (\\d) some step (.*)$"), 0, oneParamStep(), null);
        assertEquals(new Integer(1), java8StepDefinition.getParameterCount());
    }

    @Test
    public void should_calculate_parameters_count_from_body_with_two_params() throws Exception {
        java8StepDefinition = new Java8StepDefinition(Pattern.compile("^I have some step $"), 0, twoParamStep(), null);
        assertEquals(new Integer(2), java8StepDefinition.getParameterCount());
    }

    @Test
    public void should_fail_for_param_with_generic_list() throws Exception {
        try {
            new Java8StepDefinition(Pattern.compile("^I have (\\d) some step (.*)$"), 0, genericListStep(), null);
            fail();
        } catch(CucumberException expected) {
            assertEquals("Can't use java.util.List in lambda step definition. Declare a DataTable argument instead and convert manually with asList/asLists/asMap/asMaps", expected.getMessage());
        }
    }

    private StepdefBody oneParamStep() {
        return new StepdefBody.A1<String>() {
            @Override
            public void accept(String p1) {
            }
        };
    }

    private StepdefBody twoParamStep() {
        return new StepdefBody.A2<String, String>() {
            @Override
            public void accept(String p1, String p2) {
            }
        };
    }

    private StepdefBody genericListStep() {
        return new StepdefBody.A1<List>() {
            @Override
            public void accept(List p1) {
                throw new UnsupportedOperationException();
            }
        };
    }
}
