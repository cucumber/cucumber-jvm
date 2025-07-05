package io.cucumber.java8;

import io.cucumber.core.backend.CucumberBackendException;
import io.cucumber.core.backend.Glue;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static io.cucumber.core.backend.HookDefinition.HookType.BEFORE;
import static io.cucumber.java8.LambdaGlue.DEFAULT_BEFORE_ORDER;
import static io.cucumber.java8.LambdaGlue.EMPTY_TAG_EXPRESSION;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class ClosureAwareGlueRegistryTest {

    final ClosureAwareGlueRegistry registry = new ClosureAwareGlueRegistry(mock(Glue.class));

    @Test
    void should_replace_closures() {
        List<String> invocations = new ArrayList<>();
        StepDefinitionBody.A1<String> a = p1 -> {
            invocations.add("closure a with: " + p1);
        };
        StepDefinitionBody.A1<String> b = p1 -> {
            invocations.add("closure b with: " + p1);
        };
        Java8StepDefinition firstInstance = Java8StepDefinition.create("some step", StepDefinitionBody.A1.class, a);
        Java8StepDefinition secondInstance = Java8StepDefinition.create("some step", StepDefinitionBody.A1.class, b);

        registry.startRegistration();
        registry.addStepDefinition(firstInstance);
        registry.finishRegistration();

        firstInstance.invokeMethod("first");

        registry.startRegistration();
        registry.addStepDefinition(secondInstance);
        registry.finishRegistration();

        firstInstance.invokeMethod("second");

        assertThat(invocations, equalTo(Arrays.asList("closure a with: first", "closure b with: second")));
    }

    @Test
    void should_complain_about_missing_registrations() {
        StepDefinitionBody.A0 a = () -> {
        };
        Java8StepDefinition stepDefinition = Java8StepDefinition.create("some step", StepDefinitionBody.A0.class, a);

        registry.startRegistration();
        registry.addStepDefinition(stepDefinition);
        registry.finishRegistration();

        registry.startRegistration();
        CucumberBackendException exception = assertThrows(CucumberBackendException.class, registry::finishRegistration);
        assertThat(exception.getMessage(), equalTo("" +
                "Found an inconsistent number of glue registrations.\n" +
                "Previously 1 step definitions, hooks and parameter types were registered. Currently 0.\n" +
                "To optimize performance Cucumber expects glue registration to be identical for each scenario and example."));
    }

    @Test
    void should_complain_about_extra_registrations() {
        StepDefinitionBody.A0 a = () -> {
        };
        Java8StepDefinition stepDefinition = Java8StepDefinition.create("some step", StepDefinitionBody.A0.class, a);

        registry.startRegistration();
        registry.addStepDefinition(stepDefinition);
        registry.finishRegistration();

        registry.startRegistration();
        registry.addStepDefinition(stepDefinition);
        registry.addStepDefinition(stepDefinition);
        CucumberBackendException exception = assertThrows(CucumberBackendException.class, registry::finishRegistration);
        assertThat(exception.getMessage(), equalTo("" +
                "Found an inconsistent number of glue registrations.\n" +
                "Previously 1 step definitions, hooks and parameter types were registered. Currently 2.\n" +
                "To optimize performance Cucumber expects glue registration to be identical for each scenario and example."));
    }

    @Test
    void should_complain_about_mismatched_registrations() {
        Java8HookDefinition hookDefinition = new Java8HookDefinition(BEFORE, EMPTY_TAG_EXPRESSION, DEFAULT_BEFORE_ORDER,
            () -> {

            });
        registry.startRegistration();
        registry.addBeforeHookDefinition(hookDefinition);
        registry.finishRegistration();

        Java8StepDefinition stepDefinition = Java8StepDefinition.create("some step", StepDefinitionBody.A0.class,
            () -> {
            });
        registry.startRegistration();
        CucumberBackendException exception = assertThrows(CucumberBackendException.class,
            () -> registry.addStepDefinition(stepDefinition));
        assertThat(exception.getMessage(), equalTo("" +
                "Found an inconsistent glue registrations.\n" +
                "Previously the registration in slot 0 was a 'io.cucumber.java8.Java8HookDefinition'. Currently 'io.cucumber.java8.Java8StepDefinition'.\n"
                +
                "To optimize performance Cucumber expects glue registration to be identical for each scenario and example."));
    }
}
