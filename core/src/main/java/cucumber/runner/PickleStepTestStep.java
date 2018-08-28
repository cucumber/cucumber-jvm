package cucumber.runner;

import cucumber.api.TestCase;
import cucumber.runtime.DefinitionArgument;
import gherkin.pickles.PickleStep;

import java.util.Collections;
import java.util.List;

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
    boolean run(TestCase testCase, EventBus bus, Scenario scenario, boolean skipSteps) {
        boolean skipNextStep = skipSteps;

        for (HookTestStep before : beforeStepHookSteps) {
            skipNextStep |= before.run(testCase, bus,  scenario, skipSteps);
        }

        skipNextStep |= super.run(testCase, bus,  scenario, skipNextStep);

        for (HookTestStep after : afterStepHookSteps) {
            skipNextStep |=  after.run(testCase, bus,  scenario, skipSteps);
        }

        return skipNextStep;
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
