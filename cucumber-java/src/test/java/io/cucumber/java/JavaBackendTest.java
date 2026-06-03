package io.cucumber.java;

import io.cucumber.core.backend.Glue;
import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.core.backend.StepDefinition;
import io.cucumber.java.steps.Steps;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;

import java.net.URI;
import java.util.List;

import static java.lang.Thread.currentThread;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@MockitoSettings
class JavaBackendTest {

    @Captor
    ArgumentCaptor<StepDefinition> stepDefinition;

    @Mock
    private Glue glue;

    @Mock
    private ObjectFactory factory;

    private JavaBackend backend;

    @BeforeEach
    void createBackend() {
        this.backend = new JavaBackend(factory, factory, currentThread()::getContextClassLoader);
    }

    @Test
    void finds_step_definitions_by_classpath_url() {
        backend.loadGlue(glue, singletonList(URI.create("classpath:io/cucumber/java/steps")));
        backend.buildWorld();
        verify(factory).addClass(Steps.class);
    }

    @Test
    void finds_step_definitions_once_by_classpath_url() {
        backend.loadGlue(glue,
            asList(URI.create("classpath:io/cucumber/java/steps"), URI.create("classpath:io/cucumber/java/steps")));
        backend.buildWorld();
        verify(factory, times(1)).addClass(Steps.class);
    }

    @Test
    void detects_subclassed_glue_and_throws_exception() {
        Executable testMethod = () -> backend.loadGlue(glue, asList(URI.create("classpath:io/cucumber/java/steps"),
            URI.create("classpath:io/cucumber/java/incorrectlysubclassedsteps")));
        InvalidMethodException expectedThrown = assertThrows(InvalidMethodException.class, testMethod);
        assertThat(expectedThrown.getMessage(), is(equalTo(
            """
                    "io.cucumber.java.incorrectlysubclassedsteps.SubclassesSteps" extends "io.cucumber.java.steps.Steps" which declares a step definition or hook "io.cucumber.java.steps.Steps.test()".

                    It is not possible to extend classes that define step definitions or hooks.

                    If you are trying to share state between steps consider using dependency injection such as:
                         * cucumber-picocontainer
                         * cucumber-spring
                         * cucumber-jakarta-cdi
                         * ...etc
                    """)));
    }

    @Test
    void detects_repeated_annotations() {
        backend.loadGlue(glue, singletonList(URI.create("classpath:io/cucumber/java/repeatable")));
        verify(glue, times(2)).addStepDefinition(stepDefinition.capture());

        List<String> patterns = stepDefinition.getAllValues()
                .stream()
                .map(StepDefinition::getPattern)
                .collect(toList());
        assertThat(patterns, equalTo(asList("test", "test again")));

    }

}
