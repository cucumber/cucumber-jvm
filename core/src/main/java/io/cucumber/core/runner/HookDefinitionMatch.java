package io.cucumber.core.runner;

final class HookDefinitionMatch implements StepDefinitionMatch {
    private final CoreHookDefinition hookDefinition;

    HookDefinitionMatch(CoreHookDefinition hookDefinition) {
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
