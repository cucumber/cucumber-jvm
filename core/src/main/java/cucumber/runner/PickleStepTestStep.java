package cucumber.runner;

import cucumber.api.Result;
import cucumber.api.Scenario;
import cucumber.api.TestCase;
import cucumber.runtime.DefinitionArgument;
import cucumber.runtime.PickleStepDefinitionMatch;
import gherkin.pickles.PickleStep;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static cucumber.api.Result.SEVERITY;
import static java.util.Collections.max;

class PickleStepTestStep extends TestStep implements cucumber.api.PickleStepTestStep {
    private final String uri;
    private final PickleStep step;
    private final List<HookTestStep> afterStepHookSteps;
    private final List<HookTestStep> beforeStepHookSteps;
    private final PickleStepDefinitionMatch definitionMatch;

    PickleStepTestStep(String uri, PickleStep step, PickleStepDefinitionMatch definitionMatch) {
        this(uri, step, Collections.<HookTestStep>emptyList(), Collections.<HookTestStep>emptyList(), definitionMatch);
    }

    PickleStepTestStep(String uri,
                       PickleStep step,
                       List<HookTestStep> beforeStepHookSteps,
                       List<HookTestStep> afterStepHookSteps,
                       PickleStepDefinitionMatch definitionMatch
    ) {
        super(definitionMatch);
        this.uri = uri;
        this.step = step;
        this.afterStepHookSteps = afterStepHookSteps;
        this.beforeStepHookSteps = beforeStepHookSteps;
        this.definitionMatch = definitionMatch;
    }

    @Override
    Result run(TestCase testCase, EventBus bus, String language, Scenario scenario, boolean skipSteps) {
        boolean skipNextStep = skipSteps;
        List<Result> results = new ArrayList<Result>();

        for (HookTestStep before : beforeStepHookSteps) {
            Result result = before.run(testCase, bus, language, scenario, skipSteps);
            skipNextStep |= !result.is(Result.Type.PASSED);
            results.add(result);
        }

        results.add(super.run(testCase, bus, language, scenario, skipNextStep));

        for (HookTestStep after : afterStepHookSteps) {
            results.add(after.run(testCase, bus, language, scenario, skipSteps));
        }

        return max(results, SEVERITY);
    }

    List<HookTestStep> getBeforeStepHookSteps() {
        return beforeStepHookSteps;
    }

    List<HookTestStep> getAfterStepHookSteps() {
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
        return step.getLocations().get(step.getLocations().size() - 1).getLine();
    }

    @Override
    public String getStepText() {
        return step.getText();
    }

    @Override
    public List<cucumber.api.Argument> getDefinitionArgument() {
        return DefinitionArgument.createArguments(definitionMatch.getArguments());
    }

    @Override
    public List<gherkin.pickles.Argument> getStepArgument() {
        return step.getArgument();
    }

    @Override
    public String getPattern() {
        return definitionMatch.getPattern();
    }
}
