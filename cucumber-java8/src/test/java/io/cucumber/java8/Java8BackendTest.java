package io.cucumber.java8;

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
import io.cucumber.java8.steps.Steps;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Thread.currentThread;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

class Java8BackendTest {
    private MockObjectFactory factory;

    private Java8Backend backend;

    @BeforeEach
    void createBackend() {
        factory = new MockObjectFactory();
        this.backend = new Java8Backend(factory, factory, currentThread()::getContextClassLoader);
    }

    @Test
    void finds_step_definitions_by_classpath_url() {
        backend.loadGlue(new StubGlue(), singletonList(URI.create("classpath:io/cucumber/java8/steps")));
        backend.buildWorld();
        assertIterableEquals(List.of(Steps.class), factory.classes);
    }

    @Test
    void finds_step_definitions_once_by_classpath_url() {
        backend.loadGlue(new StubGlue(),
            asList(URI.create("classpath:io/cucumber/java8/steps"), URI.create("classpath:io/cucumber/java8/steps")));
        backend.buildWorld();
        assertIterableEquals(List.of(Steps.class), factory.classes);
    }

    private static class StubGlue implements Glue {
        @Override
        public void addBeforeAllHook(StaticHookDefinition beforeAllHook) {
        }

        @Override
        public void addAfterAllHook(StaticHookDefinition afterAllHook) {
        }

        @Override
        public void addStepDefinition(StepDefinition stepDefinition) {
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

    private static class MockObjectFactory implements ObjectFactory {
        List<Class<?>> classes = new ArrayList<>();
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
