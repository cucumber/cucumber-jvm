package io.cucumber.java8;

import io.cucumber.core.backend.CucumberBackendException;
import io.cucumber.core.backend.DataTableTypeDefinition;
import io.cucumber.core.backend.DefaultDataTableCellTransformerDefinition;
import io.cucumber.core.backend.DefaultDataTableEntryTransformerDefinition;
import io.cucumber.core.backend.DefaultParameterTransformerDefinition;
import io.cucumber.core.backend.DocStringTypeDefinition;
import io.cucumber.core.backend.Glue;
import io.cucumber.core.backend.HookDefinition;
import io.cucumber.core.backend.ParameterTypeDefinition;
import io.cucumber.core.backend.StepDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

final class ClosureAwareGlueRegistry implements LambdaGlueRegistry {

    private final List<AbstractGlueDefinition> definitions = new ArrayList<>();
    private int registered;
    private int expectedRegistrations = -1;

    private final Glue glue;

    ClosureAwareGlueRegistry(Glue glue) {
        this.glue = glue;
    }

    void startRegistration() {
        registered = 0;
    }

    void finishRegistration() {
        if (expectedRegistrations < 0) {
            expectedRegistrations = registered;
        } else if (expectedRegistrations != registered) {
            throw new CucumberBackendException(String.format("Found an inconsistent number of glue registrations.\n" +
                    "Previously %s step definitions, hooks and parameter types were registered. Currently %s.\n" +
                    "To optimize performance Cucumber expects glue registration to be identical for each scenario and example.",
                expectedRegistrations, registered));
        }
    }

    @Override
    public void addStepDefinition(StepDefinition stepDefinition) {
        updateOrRegister((Java8StepDefinition) stepDefinition, definitions, glue::addStepDefinition);
    }

    @Override
    public void addBeforeStepHookDefinition(HookDefinition beforeStepHook) {
        updateOrRegister((Java8HookDefinition) beforeStepHook, definitions, glue::addBeforeStepHook);

    }

    @Override
    public void addAfterStepHookDefinition(HookDefinition afterStepHook) {
        updateOrRegister((Java8HookDefinition) afterStepHook, definitions, glue::addAfterStepHook);
    }

    @Override
    public void addBeforeHookDefinition(HookDefinition beforeHook) {
        updateOrRegister((Java8HookDefinition) beforeHook, definitions, glue::addBeforeHook);
    }

    @Override
    public void addAfterHookDefinition(HookDefinition afterHook) {
        updateOrRegister((Java8HookDefinition) afterHook, definitions, glue::addAfterHook);
    }

    @Override
    public void addDocStringType(DocStringTypeDefinition docStringType) {
        updateOrRegister((Java8DocStringTypeDefinition) docStringType, definitions, glue::addDocStringType);
    }

    @Override
    public void addDataTableType(DataTableTypeDefinition dataTableType) {
        updateOrRegister((Java8DataTableTypeDefinition) dataTableType, definitions, glue::addDataTableType);
    }

    @Override
    public void addParameterType(ParameterTypeDefinition parameterType) {
        updateOrRegister((Java8ParameterTypeDefinition) parameterType, definitions, glue::addParameterType);
    }

    @Override
    public void addDefaultParameterTransformer(DefaultParameterTransformerDefinition defaultParameterTransformer) {
        updateOrRegister((Java8DefaultParameterTransformerDefinition) defaultParameterTransformer, definitions,
            glue::addDefaultParameterTransformer);
    }

    @Override
    public void addDefaultDataTableCellTransformer(
            DefaultDataTableCellTransformerDefinition defaultDataTableCellTransformer
    ) {
        updateOrRegister((Java8DefaultDataTableCellTransformerDefinition) defaultDataTableCellTransformer, definitions,
            glue::addDefaultDataTableCellTransformer);
    }

    @Override
    public void addDefaultDataTableEntryTransformer(
            DefaultDataTableEntryTransformerDefinition defaultDataTableEntryTransformer
    ) {
        updateOrRegister(
            (Java8DefaultDataTableEntryTransformerDefinition) defaultDataTableEntryTransformer,
            definitions,
            glue::addDefaultDataTableEntryTransformer);
    }

    private <T extends AbstractGlueDefinition> void updateOrRegister(
            T candidate, List<AbstractGlueDefinition> definitions, Consumer<T> register
    ) {
        if (definitions.size() <= registered) {
            definitions.add(candidate);
            register.accept(candidate);
        } else {
            AbstractGlueDefinition existing = definitions.get(registered);
            requireSameGlueClass(existing, candidate);
            existing.updateClosure(candidate);
        }
        registered++;
    }

    private <T extends AbstractGlueDefinition> void requireSameGlueClass(
            AbstractGlueDefinition existing, AbstractGlueDefinition candidate
    ) {
        if (!existing.getClass().equals(candidate.getClass())) {
            throw new CucumberBackendException(String.format("Found an inconsistent glue registrations.\n" +
                    "Previously the registration in slot %s was a '%s'. Currently '%s'.\n" +
                    "To optimize performance Cucumber expects glue registration to be identical for each scenario and example.",
                registered, existing.getClass().getName(), candidate.getClass().getName()));
        }
    }

    void disposeClosures() {
        definitions.forEach(AbstractGlueDefinition::disposeClosure);
    }
}
