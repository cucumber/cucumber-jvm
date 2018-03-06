package cucumber.runner;

import cucumber.api.HookType;
import cucumber.api.Result;
import cucumber.api.Scenario;
import cucumber.api.TestStep;
import cucumber.runtime.DefinitionMatch;
import cucumber.runtime.StepDefinitionMatch;
import gherkin.pickles.Argument;
import gherkin.pickles.PickleStep;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static cucumber.api.Result.SEVERITY;
import static java.util.Collections.max;

public class PickleTestStep extends TestStep {
    private final String uri;
    private final PickleStep step;
    private final List<TestStep> afterStepHooks;

    public PickleTestStep(String uri, PickleStep step, DefinitionMatch definitionMatch) {
        this(uri, step, Collections.<TestStep>emptyList(), definitionMatch);
    }

    public PickleTestStep(String uri, PickleStep step, List<TestStep> afterStepHooks, DefinitionMatch definitionMatch) {
        super(definitionMatch);
        this.uri = uri;
        this.step = step;
        this.afterStepHooks = afterStepHooks;
    }

    @Override
    public Result run(EventBus bus, String language, Scenario scenario, boolean skipSteps) {
        List<Result> results = new ArrayList<Result>();

        results.add(super.run(bus, language, scenario, skipSteps));

        for(TestStep after : afterStepHooks){
            results.add(after.run(bus, language, scenario, skipSteps));
        }

        return max(results, SEVERITY);
    }


    @Override
    public boolean isHook() {
        return false;
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
    public HookType getHookType() {
        throw new UnsupportedOperationException();
    }
}
