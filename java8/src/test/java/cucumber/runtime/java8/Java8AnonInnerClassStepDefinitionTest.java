package cucumber.runtime.java8;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import cucumber.api.java8.StepdefBody;
import cucumber.runtime.CucumberException;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

public class Java8AnonInnerClassStepDefinitionTest {

    @Test
    public void should_calculate_parameters_count_from_body_with_one_param() throws Exception {
        Java8StepDefinition java8StepDefinition = new Java8StepDefinition("^I have (\\d) some step (.*)$", 0, StepdefBody.A1.class, oneParamStep());
        assertEquals(Integer.valueOf(1), java8StepDefinition.getParameterCount());
    }

    @Test
    public void should_calculate_parameters_count_from_body_with_two_params() throws Exception {
        Java8StepDefinition java8StepDefinition = new Java8StepDefinition("^I have some step $", 0, StepdefBody.A2.class, twoParamStep());
        assertEquals(Integer.valueOf(2), java8StepDefinition.getParameterCount());
    }

    @Test
    public void should_fail_for_param_with_non_generic_list() throws Exception {
        try {
            new Java8StepDefinition("^I have (\\d) some step (.*)$", 0, StepdefBody.A1.class, nonGenericListStep());
            fail();
        } catch (CucumberException expected) {
            assertEquals("Can't use java.util.List in lambda step definition. Declare a DataTable argument instead and convert manually with asList/asLists/asMap/asMaps", expected.getMessage());
        }
    }

    @Test
    public void should_fail_for_param_with_generic_list() throws Exception {
        try {
            new Java8StepDefinition("^I have (\\d) some step (.*)$", 0, StepdefBody.A1.class, genericListStep());
            fail();
        } catch (CucumberException expected) {
            assertEquals("Can't use java.util.List in lambda step definition. Declare a DataTable argument instead and convert manually with asList/asLists/asMap/asMaps", expected.getMessage());
        }
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
