package io.cucumber.java8;

import io.cucumber.core.backend.CucumberBackendException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.Is.isA;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Java8LambdaStepDefinitionTest {

    @Test
    void should_calculate_parameters_count_from_body_with_one_param() {
        StepDefinitionBody.A1<String> body = p1 -> {
        };
        Java8StepDefinition stepDefinition = Java8StepDefinition.create("some step", StepDefinitionBody.A1.class, body);
        assertThat(stepDefinition.parameterInfos().size(), is(equalTo(1)));
    }

    @Test
    void should_calculate_parameters_count_from_body_with_two_params() {
        StepDefinitionBody.A2<String, String> body = (p1, p2) -> {
        };
        Java8StepDefinition stepDefinition = Java8StepDefinition.create("some step", StepDefinitionBody.A2.class, body);
        assertThat(stepDefinition.parameterInfos().size(), is(equalTo(2)));
    }

    @Test
    void should_resolve_type_to_object() {
        StepDefinitionBody.A1 body = (p1) -> {
        };
        Java8StepDefinition stepDefinition = Java8StepDefinition.create("some step", StepDefinitionBody.A1.class, body);

        assertThat(stepDefinition.parameterInfos().get(0).getType(), isA((Object.class)));
    }

    @Test
    void should_fail_for_param_with_non_generic_list() {
        StepDefinitionBody.A1<List> body = p1 -> {
        };
        Java8StepDefinition stepDefinition = Java8StepDefinition.create("some step", StepDefinitionBody.A1.class, body);

        Executable testMethod = () -> stepDefinition.parameterInfos().get(0).getTypeResolver().resolve();
        CucumberBackendException actualThrown = assertThrows(CucumberBackendException.class, testMethod);
        assertThat("Unexpected exception message", actualThrown.getMessage(), is(equalTo(
            "Can't use java.util.List in lambda step definition \"some step\". " +
                    "Declare a DataTable or DocString argument instead and convert " +
                    "manually with 'asList/asLists/asMap/asMaps' and 'convert' respectively")));
    }

    @Test
    void should_fail_for_param_with_generic_list() {
        StepDefinitionBody.A1<List<String>> body = p1 -> {
        };
        Java8StepDefinition stepDefinition = Java8StepDefinition.create("some step", StepDefinitionBody.A1.class, body);

        Executable testMethod = () -> stepDefinition.parameterInfos().get(0).getTypeResolver().resolve();
        CucumberBackendException actualThrown = assertThrows(CucumberBackendException.class, testMethod);
        assertThat("Unexpected exception message", actualThrown.getMessage(), is(equalTo(
            "Can't use java.util.List in lambda step definition \"some step\". " +
                    "Declare a DataTable or DocString argument instead and convert " +
                    "manually with 'asList/asLists/asMap/asMaps' and 'convert' respectively")));
    }

}
