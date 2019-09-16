package io.cucumber.java8;

import io.cucumber.core.backend.DataTableTypeDefinition;
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

    void addDocStringType(DocStringTypeDefinition docStringTypeDefinition);

    void addDataTableType(DataTableTypeDefinition dataTableTypeDefinition);

    void addParameterType(ParameterTypeDefinition parameterTypeDefinition);
}
