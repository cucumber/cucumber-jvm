package cucumber.api;

import cucumber.api.event.TestCaseFinished;
import cucumber.api.event.TestCaseStarted;
import cucumber.runner.EventBus;
import cucumber.runner.PickleTestStep;
import cucumber.runtime.ScenarioImpl;
import gherkin.events.PickleEvent;
import gherkin.pickles.PickleLocation;
import gherkin.pickles.PickleTag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TestCase {
    private final PickleEvent pickleEvent;
    private final List<PickleTestStep> testSteps;
    private final boolean dryRun;
    private final List<Step> beforeHooks;
    private final List<Step> afterHooks;

    public TestCase(List<PickleTestStep> testSteps, List<Step> beforeHooks, List<Step> afterHooks, PickleEvent pickleEvent, boolean dryRun) {
        this.testSteps = testSteps;
        this.beforeHooks = beforeHooks;
        this.afterHooks = afterHooks;
        this.pickleEvent = pickleEvent;
        this.dryRun = dryRun;
    }

    /**
     * Creates a new instance of a test case.
     *
     * @param testSteps   of the test case
     * @param pickleEvent the pickle executed by this test case
     * @deprecated not part of the public api
     */
    @Deprecated
    public TestCase(List<PickleTestStep> testSteps, PickleEvent pickleEvent) {
        this(testSteps, Collections.<Step>emptyList(), Collections.<Step>emptyList(), pickleEvent, false);
    }

    /**
     * Creates a new instance of a test case.
     *
     * @param testSteps   of the test case
     * @param pickleEvent the pickle executed by this test case
     * @param dryRun      skip execution of the test steps
     * @deprecated not part of the public api
     */
    @Deprecated
    public TestCase(List<PickleTestStep> testSteps, PickleEvent pickleEvent, boolean dryRun) {
        this(testSteps, Collections.<Step>emptyList(), Collections.<Step>emptyList(), pickleEvent, dryRun);
    }

    /**
     * Executes the test case.
     *
     * @param bus to which events should be broadcast
     * @deprecated not part of the public api
     */
    @Deprecated
    public void run(EventBus bus) {
        boolean skipNextStep = this.dryRun;
        Long startTime = bus.getTime();
        bus.send(new TestCaseStarted(startTime, this));
        ScenarioImpl scenarioResult = new ScenarioImpl(bus, pickleEvent);

        for (Step before : beforeHooks) {
            Result stepResult = before.run(bus, pickleEvent.pickle.getLanguage(), scenarioResult, dryRun);
            skipNextStep |= !stepResult.is(Result.Type.PASSED);
            scenarioResult.add(stepResult);
        }

        for (Step step : testSteps) {
            Result stepResult = step.run(bus, pickleEvent.pickle.getLanguage(), scenarioResult, skipNextStep);
            skipNextStep |= !stepResult.is(Result.Type.PASSED);
            scenarioResult.add(stepResult);
        }

        for (Step after : afterHooks) {
            Result stepResult = after.run(bus, pickleEvent.pickle.getLanguage(), scenarioResult, dryRun);
            scenarioResult.add(stepResult);
        }

        Long stopTime = bus.getTime();
        bus.send(new TestCaseFinished(stopTime, this, new Result(scenarioResult.getStatus(), stopTime - startTime, scenarioResult.getError())));
    }

    public List<Step> getTestSteps() {
        List<Step> steps = new ArrayList<Step>();
        steps.addAll(beforeHooks);
        for (PickleTestStep step : testSteps) {
            steps.addAll(step.getBeforeStepHookSteps());
            steps.add(step);
            steps.addAll(step.getAfterStepHookSteps());
        }
        steps.addAll(afterHooks);
        return steps;
    }

    public String getName() {
        return pickleEvent.pickle.getName();
    }

    public String getScenarioDesignation() {
        return fileColonLine(pickleEvent.pickle.getLocations().get(0)) + " # " + getName();
    }

    public String getUri() {
        return pickleEvent.uri;
    }

    public int getLine() {
        return pickleEvent.pickle.getLocations().get(0).getLine();
    }

    private String fileColonLine(PickleLocation location) {
        return pickleEvent.uri + ":" + location.getLine();
    }

    public List<PickleTag> getTags() {
        return pickleEvent.pickle.getTags();
    }
}
