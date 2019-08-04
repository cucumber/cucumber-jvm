package io.cucumber.java8;

import io.cucumber.core.exception.CucumberException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.Is.isA;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class Java8LambdaStepDefinitionTest {

    @Test
    public void should_calculate_parameters_count_from_body_with_one_param() {
        StepdefBody.A1<String> body = p1 -> {
        };
        Java8StepDefinition stepDefinition = Java8StepDefinition.create("some step", StepdefBody.A1.class, body);
        assertThat(stepDefinition.parameterInfos().size(), is(equalTo(1)));
    }

    @Test
    public void should_calculate_parameters_count_from_body_with_two_params() {
        StepdefBody.A2<String, String> body = (p1, p2) -> {
        };
        Java8StepDefinition stepDefinition = Java8StepDefinition.create("some step", StepdefBody.A2.class, body);
        assertThat(stepDefinition.parameterInfos().size(), is(equalTo(2)));
    }

    @Test
    public void should_resolve_type_to_object() {
        StepdefBody.A1 body = (p1) -> {
        };
        Java8StepDefinition stepDefinition = Java8StepDefinition.create("some step", StepdefBody.A1.class, body);

        assertThat(stepDefinition.parameterInfos().get(0).getType(), isA((Object.class)));
    }

    @Test
    public void should_fail_for_param_with_non_generic_list() {
        StepdefBody.A1<List> body = p1 -> {
        };
        Java8StepDefinition stepDefinition = Java8StepDefinition.create("some step", StepdefBody.A1.class, body);

        final Executable testMethod = () -> stepDefinition.parameterInfos().get(0).getTypeResolver().resolve();
        final CucumberException actualThrown = assertThrows(CucumberException.class, testMethod);
        assertThat("Unexpected exception message", actualThrown.getMessage(), is(equalTo(
            "Can't use java.util.List in lambda step definition \"some step\". Declare a DataTable argument instead and convert manually with asList/asLists/asMap/asMaps"
        )));
    }

    @Test
    public void should_fail_for_param_with_generic_list() {
        StepdefBody.A1<List<String>> body = p1 -> {
        };
        Java8StepDefinition stepDefinition = Java8StepDefinition.create("some step", StepdefBody.A1.class, body);

        final Executable testMethod = () -> stepDefinition.parameterInfos().get(0).getTypeResolver().resolve();
        final CucumberException actualThrown = assertThrows(CucumberException.class, testMethod);
        assertThat("Unexpected exception message", actualThrown.getMessage(), is(equalTo(
            "Can't use java.util.List in lambda step definition \"some step\". Declare a DataTable argument instead and convert manually with asList/asLists/asMap/asMaps"
        )));
    }

}
