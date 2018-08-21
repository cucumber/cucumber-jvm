package cucumber.runner;

import cucumber.api.Scenario;
import cucumber.runtime.HookDefinition;
import cucumber.runtime.StepDefinitionMatch;

class HookDefinitionMatch implements StepDefinitionMatch {
    private final HookDefinition hookDefinition;

    HookDefinitionMatch(HookDefinition hookDefinition) {
        this.hookDefinition = hookDefinition;
    }

    @Override
    public void runStep(String language, Scenario scenario) throws Throwable {
        hookDefinition.execute(scenario);
    }

    @Override
    public void dryRunStep(String language, Scenario scenario) throws Throwable {
        // Do nothing
    }

    @Override
    public String getCodeLocation() {
        return hookDefinition.getLocation(false);
    }

}
