package cucumber.runner;

import cucumber.api.Scenario;
import cucumber.runtime.HookDefinition;
import cucumber.runtime.StepDefinitionMatch;

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
