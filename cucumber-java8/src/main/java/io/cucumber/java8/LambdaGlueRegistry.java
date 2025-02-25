package io.cucumber.java8;

import io.cucumber.core.backend.CucumberBackendException;
import io.cucumber.core.backend.DataTableTypeDefinition;
import io.cucumber.core.backend.DefaultDataTableCellTransformerDefinition;
import io.cucumber.core.backend.DefaultDataTableEntryTransformerDefinition;
import io.cucumber.core.backend.DefaultParameterTransformerDefinition;
import io.cucumber.core.backend.DocStringTypeDefinition;
import io.cucumber.core.backend.HookDefinition;
import io.cucumber.core.backend.ParameterTypeDefinition;
import io.cucumber.core.backend.StepDefinition;

interface LambdaGlueRegistry {

    LambdaGlueRegistry CLOSED = new ClosedLambdaGlueRegistry();
    ThreadLocal<LambdaGlueRegistry> INSTANCE = ThreadLocal.withInitial(() -> CLOSED);

    void addStepDefinition(StepDefinition stepDefinition);

    void addBeforeStepHookDefinition(HookDefinition beforeStepHook);

    void addAfterStepHookDefinition(HookDefinition afterStepHook);

    void addBeforeHookDefinition(HookDefinition beforeHook);

    void addAfterHookDefinition(HookDefinition afterHook);

    void addDocStringType(DocStringTypeDefinition docStringType);

    void addDataTableType(DataTableTypeDefinition dataTableType);

    void addParameterType(ParameterTypeDefinition parameterType);

    void addDefaultParameterTransformer(DefaultParameterTransformerDefinition defaultParameterTransformer);

    void addDefaultDataTableCellTransformer(DefaultDataTableCellTransformerDefinition defaultDataTableCellTransformer);

    void addDefaultDataTableEntryTransformer(
            DefaultDataTableEntryTransformerDefinition defaultDataTableEntryTransformer
    );

    class ClosedLambdaGlueRegistry implements LambdaGlueRegistry {

        private static CucumberBackendException createRegistryIsClosedException(Class<?> aClass) {
            return new CucumberBackendException(aClass.getName()
                    + " was initialized either without an active scenario or after a scenario already started execution.");
        }

        @Override
        public void addStepDefinition(StepDefinition stepDefinition) {
            throw createRegistryIsClosedException(stepDefinition.getClass());
        }

        @Override
        public void addBeforeStepHookDefinition(HookDefinition beforeStepHook) {
            throw createRegistryIsClosedException(beforeStepHook.getClass());
        }

        @Override
        public void addAfterStepHookDefinition(HookDefinition afterStepHook) {
            throw createRegistryIsClosedException(afterStepHook.getClass());

        }

        @Override
        public void addBeforeHookDefinition(HookDefinition beforeHook) {
            throw createRegistryIsClosedException(beforeHook.getClass());

        }

        @Override
        public void addAfterHookDefinition(HookDefinition afterHook) {
            throw createRegistryIsClosedException(afterHook.getClass());
        }

        @Override
        public void addDocStringType(DocStringTypeDefinition docStringType) {
            throw createRegistryIsClosedException(docStringType.getClass());
        }

        @Override
        public void addDataTableType(DataTableTypeDefinition dataTableType) {
            throw createRegistryIsClosedException(dataTableType.getClass());
        }

        @Override
        public void addParameterType(ParameterTypeDefinition parameterType) {
            throw createRegistryIsClosedException(parameterType.getClass());
        }

        @Override
        public void addDefaultParameterTransformer(DefaultParameterTransformerDefinition defaultParameterTransformer) {
            throw createRegistryIsClosedException(defaultParameterTransformer.getClass());
        }

        @Override
        public void addDefaultDataTableCellTransformer(
                DefaultDataTableCellTransformerDefinition defaultDataTableCellTransformer
        ) {
            throw createRegistryIsClosedException(defaultDataTableCellTransformer.getClass());
        }

        @Override
        public void addDefaultDataTableEntryTransformer(
                DefaultDataTableEntryTransformerDefinition defaultDataTableEntryTransformer
        ) {
            throw createRegistryIsClosedException(defaultDataTableEntryTransformer.getClass());
        }
    }
}
