package io.cucumber.core.backend;

import io.cucumber.core.stepexpression.TypeRegistry;
import org.apiguardian.api.API;

import java.util.function.Function;

@API(status = API.Status.STABLE)
public interface Glue {

    void addStepDefinition(Function<TypeRegistry, StepDefinition> stepDefinition) throws DuplicateStepDefinitionException;

    void addBeforeHook(HookDefinition hookDefinition);

    void addAfterHook(HookDefinition hookDefinition);

    void addBeforeStepHook(HookDefinition beforeStepHook);

    void addAfterStepHook(HookDefinition hookDefinition);

}
