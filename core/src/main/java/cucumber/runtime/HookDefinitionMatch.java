package cucumber.runtime;

import cucumber.api.Scenario;

public class HookDefinitionMatch implements StepDefinitionMatch {
    private final HookDefinition hookDefinition;

    public HookDefinitionMatch(HookDefinition hookDefinition) {
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
