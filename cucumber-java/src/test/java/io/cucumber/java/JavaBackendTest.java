package io.cucumber.java;

import io.cucumber.core.backend.DataTableTypeDefinition;
import io.cucumber.core.backend.DefaultDataTableCellTransformerDefinition;
import io.cucumber.core.backend.DefaultDataTableEntryTransformerDefinition;
import io.cucumber.core.backend.DefaultParameterTransformerDefinition;
import io.cucumber.core.backend.DocStringTypeDefinition;
import io.cucumber.core.backend.Glue;
import io.cucumber.core.backend.HookDefinition;
import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.core.backend.ParameterTypeDefinition;
import io.cucumber.core.backend.StaticHookDefinition;
import io.cucumber.core.backend.StepDefinition;
import io.cucumber.java.steps.Steps;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Thread.currentThread;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JavaBackendTest {
    private MockObjectFactory factory;

    private JavaBackend backend;

    @BeforeEach
    void createBackend() {
        factory = new MockObjectFactory();
        this.backend = new JavaBackend(factory, factory, currentThread()::getContextClassLoader);
    }

    @Test
    void finds_step_definitions_by_classpath_url() {
        MockGlue glue = new MockGlue();

        backend.loadGlue(glue, singletonList(URI.create("classpath:io/cucumber/java/steps")));
        backend.buildWorld();

        assertIterableEquals(List.of(Steps.class), factory.classes);
    }

    @Test
    void finds_step_definitions_once_by_classpath_url() {
        MockGlue glue = new MockGlue();

        backend.loadGlue(glue,
            asList(URI.create("classpath:io/cucumber/java/steps"), URI.create("classpath:io/cucumber/java/steps")));
        backend.buildWorld();

        assertIterableEquals(List.of(Steps.class), factory.classes);
    }

    @Test
    void detects_subclassed_glue_and_throws_exception() {
        MockGlue glue = new MockGlue();
        Executable testMethod = () -> backend.loadGlue(glue, asList(URI.create("classpath:io/cucumber/java/steps"),
            URI.create("classpath:io/cucumber/java/incorrectlysubclassedsteps")));
        InvalidMethodException expectedThrown = assertThrows(InvalidMethodException.class, testMethod);
        assertThat(expectedThrown.getMessage(), is(equalTo(
            "You're not allowed to extend classes that define Step Definitions or hooks. class io.cucumber.java.incorrectlysubclassedsteps.SubclassesSteps extends class io.cucumber.java.steps.Steps")));
    }

    @Test
    void detects_repeated_annotations() {
        MockGlue glue = new MockGlue();

        backend.loadGlue(glue, singletonList(URI.create("classpath:io/cucumber/java/repeatable")));

        assertEquals(2, glue.stepDefinitions.size());
        List<String> patterns = glue.stepDefinitions
                .stream()
                .map(StepDefinition::getPattern)
                .collect(toList());
        assertThat(patterns, equalTo(asList("test", "test again")));

    }

    private static class MockGlue implements Glue {
        final List<StepDefinition> stepDefinitions = new ArrayList<>();

        @Override
        public void addBeforeAllHook(StaticHookDefinition beforeAllHook) {
        }

        @Override
        public void addAfterAllHook(StaticHookDefinition afterAllHook) {
        }

        @Override
        public void addStepDefinition(StepDefinition stepDefinition) {
            stepDefinitions.add(stepDefinition);
        }

        @Override
        public void addBeforeHook(HookDefinition beforeHook) {
        }

        @Override
        public void addAfterHook(HookDefinition afterHook) {
        }

        @Override
        public void addBeforeStepHook(HookDefinition beforeStepHook) {
        }

        @Override
        public void addAfterStepHook(HookDefinition afterStepHook) {
        }

        @Override
        public void addParameterType(ParameterTypeDefinition parameterType) {
        }

        @Override
        public void addDataTableType(DataTableTypeDefinition dataTableType) {
        }

        @Override
        public void addDefaultParameterTransformer(DefaultParameterTransformerDefinition defaultParameterTransformer) {
        }

        @Override
        public void addDefaultDataTableEntryTransformer(
                DefaultDataTableEntryTransformerDefinition defaultDataTableEntryTransformer
        ) {
        }

        @Override
        public void addDefaultDataTableCellTransformer(
                DefaultDataTableCellTransformerDefinition defaultDataTableCellTransformer
        ) {
        }

        @Override
        public void addDocStringType(DocStringTypeDefinition docStringType) {
        }
    }

    private class MockObjectFactory implements ObjectFactory {
        final List<Class<?>> classes = new ArrayList<>();

        @Override
        public boolean addClass(Class<?> glueClass) {
            return classes.add(glueClass);
        }

        @Override
        public <T> T getInstance(Class<T> glueClass) {
            return null;
        }

        @Override
        public void start() {

        }

        @Override
        public void stop() {

        }
    }
}
