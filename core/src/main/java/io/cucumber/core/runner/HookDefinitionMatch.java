package io.cucumber.core.runner;

import io.cucumber.core.api.Scenario;
import io.cucumber.core.backend.HookDefinition;
import io.cucumber.core.backend.StepDefinitionMatch;

final class HookDefinitionMatch implements StepDefinitionMatch {
    private final HookDefinition hookDefinition;

    HookDefinitionMatch(HookDefinition hookDefinition) {
        this.hookDefinition = hookDefinition;
    }

    @Override
    public void runStep(Scenario scenario) throws Throwable {
        hookDefinition.execute(scenario);
    }

    @Override
    public void dryRunStep(Scenario scenario) {
        // Do nothing
    }

    @Override
    public String getCodeLocation() {
        return hookDefinition.getLocation(false);
    }

}
