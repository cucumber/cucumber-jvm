package cucumber.runtime.java;

import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;

import cucumber.api.java8.StepdefBody;

public class Java8StepDefinitionTest {

    private Java8StepDefinition java8StepDefinition;

    @Test
    public void should_calculate_parameters_count_by_using_only_step_method_parameters_definition() throws Exception {
        java8StepDefinition = new Java8StepDefinition(Pattern.compile("^I have (\\d) some step (.*)$"), 0, oneParamStep(), null);
        Assert.assertEquals(new Integer(1), java8StepDefinition.getParameterCount());

        java8StepDefinition = new Java8StepDefinition(Pattern.compile("^I have some step $"), 0, twoParamStep(), null);
        Assert.assertEquals(new Integer(2), java8StepDefinition.getParameterCount());
    }

    private StepdefBody oneParamStep() {
        return new StepdefBody.A1<String>() {
            public void accept(String p1) {
            }
        };
    }

    private StepdefBody twoParamStep() {
        return new StepdefBody.A2<String, String>() {
            public void accept(String p1, String p2) {
            }
        };
    }

}
