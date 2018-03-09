package cucumber.runner;

import cucumber.api.Result;
import cucumber.api.Scenario;
import cucumber.runtime.DefinitionMatch;
import cucumber.runtime.StepDefinitionMatch;
import gherkin.pickles.Argument;
import gherkin.pickles.PickleStep;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static cucumber.api.Result.SEVERITY;
import static java.util.Collections.emptyList;
import static java.util.Collections.max;

class PickleTestStep extends Step implements cucumber.api.TestStep {
    private final String uri;
    private final PickleStep step;
    private final List<HookStep> afterStepHookSteps;
    private final List<HookStep> beforeStepHookSteps;

    PickleTestStep(String uri, PickleStep step, DefinitionMatch definitionMatch) {
        this(uri, Collections.<HookStep>emptyList(), step, Collections.<HookStep>emptyList(), definitionMatch);
    }

    PickleTestStep(String uri, List<HookStep> beforeStepHookSteps, PickleStep step, List<HookStep> afterStepHookSteps, DefinitionMatch definitionMatch) {
        super(definitionMatch);
        this.uri = uri;
        this.step = step;
        this.afterStepHookSteps = afterStepHookSteps;
        this.beforeStepHookSteps = beforeStepHookSteps;
    }

    @Override
    Result run(EventBus bus, String language, Scenario scenario, boolean skipSteps) {
        boolean skipNextStep = skipSteps;
        List<Result> results = new ArrayList<Result>();

        for (HookStep before : beforeStepHookSteps) {
            Result result = before.run(bus, language, scenario, skipSteps);
            skipNextStep |= !result.is(Result.Type.PASSED);
            results.add(result);
        }

        results.add(super.run(bus, language, scenario, skipNextStep));

        for (HookStep after : afterStepHookSteps) {
            results.add(after.run(bus, language, scenario, skipSteps));
        }

        return max(results, SEVERITY);
    }

    List<HookStep> getBeforeStepHookSteps() {
        return beforeStepHookSteps;
    }

    List<HookStep> getAfterStepHookSteps() {
        return afterStepHookSteps;
    }

    @Override
    public PickleStep getPickleStep() {
        return step;
    }

    @Override
    public String getStepLocation() {
        return uri + ":" + Integer.toString(getStepLine());
    }

    @Override
    public int getStepLine() {
        return StepDefinitionMatch.getStepLine(step);
    }

    @Override
    public String getStepText() {
        return step.getText();
    }

    @Override
    public List<Argument> getStepArgument() {
        return step.getArgument();
    }

    @Override
    public String getPattern() {
        return definitionMatch.getPattern();
    }
}
