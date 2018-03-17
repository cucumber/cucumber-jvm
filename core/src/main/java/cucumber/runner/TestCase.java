package cucumber.runner;

import cucumber.api.Result;
import cucumber.api.TestStep;
import cucumber.api.event.TestCaseFinished;
import cucumber.api.event.TestCaseStarted;
import cucumber.runtime.ScenarioImpl;
import gherkin.events.PickleEvent;
import gherkin.pickles.PickleLocation;
import gherkin.pickles.PickleTag;

import java.util.ArrayList;
import java.util.List;

class TestCase implements cucumber.api.TestCase {
    private final PickleEvent pickleEvent;
    private final List<PickleStepTestStep> testSteps;
    private final boolean dryRun;
    private final List<HookTestStep> beforeHooks;
    private final List<HookTestStep> afterHooks;

    public TestCase(List<PickleStepTestStep> testSteps,
                    List<HookTestStep> beforeHooks,
                    List<HookTestStep> afterHooks,
                    PickleEvent pickleEvent,
                    boolean dryRun) {
        this.testSteps = testSteps;
        this.beforeHooks = beforeHooks;
        this.afterHooks = afterHooks;
        this.pickleEvent = pickleEvent;
        this.dryRun = dryRun;
    }

    void run(EventBus bus) {
        boolean skipNextStep = this.dryRun;
        Long startTime = bus.getTime();
        bus.send(new TestCaseStarted(startTime, this));
        ScenarioImpl scenarioResult = new ScenarioImpl(bus, pickleEvent);

        for (HookTestStep before : beforeHooks) {
            Result stepResult = before.run(bus, pickleEvent.pickle.getLanguage(), scenarioResult, dryRun);
            skipNextStep |= !stepResult.is(Result.Type.PASSED);
            scenarioResult.add(stepResult);
        }

        for (PickleStepTestStep step : testSteps) {
            Result stepResult = step.run(bus, pickleEvent.pickle.getLanguage(), scenarioResult, skipNextStep);
            skipNextStep |= !stepResult.is(Result.Type.PASSED);
            scenarioResult.add(stepResult);
        }

        for (HookTestStep after : afterHooks) {
            Result stepResult = after.run(bus, pickleEvent.pickle.getLanguage(), scenarioResult, dryRun);
            scenarioResult.add(stepResult);
        }

        Long stopTime = bus.getTime();
        bus.send(new TestCaseFinished(stopTime, this, new Result(scenarioResult.getStatus(), stopTime - startTime, scenarioResult.getError())));
    }

    @Override
    public List<TestStep> getTestSteps() {
        List<TestStep> testSteps = new ArrayList<TestStep>();
        testSteps.addAll(beforeHooks);
        for (PickleStepTestStep step : this.testSteps) {
            testSteps.addAll(step.getBeforeStepHookSteps());
            testSteps.add(step);
            testSteps.addAll(step.getAfterStepHookSteps());
        }
        testSteps.addAll(afterHooks);
        return testSteps;
    }

    @Override
    public String getName() {
        return pickleEvent.pickle.getName();
    }

    @Override
    public String getScenarioDesignation() {
        return fileColonLine(pickleEvent.pickle.getLocations().get(0)) + " # " + getName();
    }

    @Override
    public String getUri() {
        return pickleEvent.uri;
    }

    @Override
    public int getLine() {
        return pickleEvent.pickle.getLocations().get(0).getLine();
    }

    private String fileColonLine(PickleLocation location) {
        return pickleEvent.uri + ":" + location.getLine();
    }

    @Override
    public List<PickleTag> getTags() {
        return pickleEvent.pickle.getTags();
    }
}
