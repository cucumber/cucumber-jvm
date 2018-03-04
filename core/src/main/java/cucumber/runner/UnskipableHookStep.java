package cucumber.runner;

import cucumber.api.HookType;
import cucumber.api.Result;
import cucumber.api.Result.Type;
import cucumber.api.Scenario;
import cucumber.api.TestCase.SkipStatus;
import cucumber.runtime.DefinitionMatch;

public class UnskipableHookStep extends HookStep {

    public UnskipableHookStep(HookType hookType, DefinitionMatch definitionMatch) {
        super(hookType, definitionMatch);
    }

    @Override
    protected Type executeStep(String language, Scenario scenario, SkipStatus skipSteps) throws Throwable {
        definitionMatch.runStep(language, scenario);
        return Result.Type.PASSED;
    }
}
