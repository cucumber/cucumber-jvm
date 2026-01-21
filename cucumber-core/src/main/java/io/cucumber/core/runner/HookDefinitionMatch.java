package io.cucumber.core.runner;

import io.cucumber.core.backend.CucumberBackendException;
import io.cucumber.core.backend.CucumberInvocationTargetException;
import io.cucumber.core.backend.TestCaseState;
import io.cucumber.core.exception.CucumberException;

import static io.cucumber.core.runner.StackManipulation.removeFrameworkFrames;

final class HookDefinitionMatch implements StepDefinitionMatch {

    private final CoreHookDefinition hookDefinition;

    HookDefinitionMatch(CoreHookDefinition hookDefinition) {
        this.hookDefinition = hookDefinition;
    }

    @Override
    public void runStep(TestCaseState state) throws Throwable {
        try {
            hookDefinition.execute(state);
        } catch (CucumberBackendException e) {
            throw couldNotInvokeHook(e);
        } catch (CucumberInvocationTargetException e) {
            throw removeFrameworkFrames(e);
        }
    }

    private Throwable couldNotInvokeHook(CucumberBackendException e) {
        return new CucumberException(String.format("" +
                "Could not invoke hook defined at '%s'.\n" +
                // TODO: Add doc URL
                "It appears there was a problem with the hook definition.",
            hookDefinition.getLocation()), e);
    }

    @Override
    public void dryRunStep(TestCaseState state) {
        // Do nothing
    }

    @Override
    public String getCodeLocation() {
        return hookDefinition.getLocation();
    }

    CoreHookDefinition getHookDefinition() {
        return hookDefinition;
    }

}
