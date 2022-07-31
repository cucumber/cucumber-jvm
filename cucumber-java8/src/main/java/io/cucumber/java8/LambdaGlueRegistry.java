package io.cucumber.java8;

import io.cucumber.core.backend.DataTableTypeDefinition;
import io.cucumber.core.backend.DefaultDataTableCellTransformerDefinition;
import io.cucumber.core.backend.DefaultDataTableEntryTransformerDefinition;
import io.cucumber.core.backend.DefaultParameterTransformerDefinition;
import io.cucumber.core.backend.DocStringTypeDefinition;
import io.cucumber.core.backend.HookDefinition;
import io.cucumber.core.backend.ParameterTypeDefinition;
import io.cucumber.core.backend.StepDefinition;

interface LambdaGlueRegistry {

    ThreadLocal<LambdaGlueRegistry> INSTANCE = new ThreadLocal<>();

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

}
