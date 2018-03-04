package cucumber.runner;

import cucumber.api.HookType;
import cucumber.api.Result;
import cucumber.api.Result.Type;
import cucumber.api.Scenario;
import cucumber.api.TestCase.SkipStatus;
import cucumber.runtime.DefinitionMatch;

public class SkipableHookStep extends HookStep {

    public SkipableHookStep(HookType hookType, DefinitionMatch definitionMatch) {
        super(hookType, definitionMatch);
    }

    @Override
    protected Type executeStep(String language, Scenario scenario, SkipStatus skipSteps) throws Throwable {
        if (skipSteps != SkipStatus.SKIP_ALL_SKIPABLE) {
            definitionMatch.runStep(language, scenario);
            return Result.Type.PASSED;
        } else {
            definitionMatch.dryRunStep(language, scenario);
            return Result.Type.SKIPPED;
        }
    }
}
