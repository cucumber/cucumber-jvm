package io.cucumber.java8;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class Java8LambdaStepDefinitionTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void should_calculate_parameters_count_from_body_with_one_param() {
        StepdefBody.A1<String> body = p1 -> {
        };
        Java8StepDefinition stepDefinition = Java8StepDefinition.create("some step", StepdefBody.A1.class, body);
        assertEquals(1, stepDefinition.parameterInfos().size());
    }

    @Test
    public void should_calculate_parameters_count_from_body_with_two_params() {
        StepdefBody.A2<String, String> body = (p1, p2) -> {
        };
        Java8StepDefinition stepDefinition = Java8StepDefinition.create("some step", StepdefBody.A2.class, body);
        assertEquals(2, stepDefinition.parameterInfos().size());
    }

    @Test
    public void should_resolve_type_to_object() {
        StepdefBody.A1 body = (p1) -> {
        };
        Java8StepDefinition stepDefinition = Java8StepDefinition.create("some step", StepdefBody.A1.class, body);

        assertEquals(Object.class, stepDefinition.parameterInfos().get(0).getType());
    }

    @Test
    public void should_fail_for_param_with_non_generic_list() {
        expectedException.expectMessage("Can't use java.util.List in lambda step definition \"some step\". Declare a DataTable argument instead and convert manually with asList/asLists/asMap/asMaps");

        StepdefBody.A1<List> body = p1 -> {
        };
        Java8StepDefinition stepDefinition = Java8StepDefinition.create("some step", StepdefBody.A1.class, body);
        stepDefinition.parameterInfos().get(0).getTypeResolver().resolve();
    }

    @Test
    public void should_fail_for_param_with_generic_list() {
        expectedException.expectMessage("Can't use java.util.List in lambda step definition \"some step\". Declare a DataTable argument instead and convert manually with asList/asLists/asMap/asMaps");

        StepdefBody.A1<List<String>> body = p1 -> {
        };
        Java8StepDefinition stepDefinition = Java8StepDefinition.create("some step", StepdefBody.A1.class, body);
        stepDefinition.parameterInfos().get(0).getTypeResolver().resolve();
    }
}
