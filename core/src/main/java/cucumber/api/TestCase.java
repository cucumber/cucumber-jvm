package cucumber.api;

import cucumber.api.event.TestCaseFinished;
import cucumber.api.event.TestCaseStarted;
import cucumber.runner.EventBus;
import cucumber.runtime.ScenarioImpl;
import gherkin.events.PickleEvent;
import gherkin.pickles.PickleLocation;
import gherkin.pickles.PickleTag;

import java.util.List;

public class TestCase {
    private final PickleEvent pickleEvent;
    private final List<TestStep> testSteps;
    private final boolean dryRun;
    public enum SkipStatus {
        RUN_ALL,
        RUN_HOOKS,
        SKIP_ALL_SKIPABLE
    };

    /**
     * Creates a new instance of a test case.
     *
     * @param testSteps   of the test case
     * @param pickleEvent the pickle executed by this test case
     * @deprecated not part of the public api
     */
    @Deprecated
    public TestCase(List<TestStep> testSteps, PickleEvent pickleEvent) {
        this(testSteps, pickleEvent, false);
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
    public TestCase(List<TestStep> testSteps, PickleEvent pickleEvent, boolean dryRun) {
        this.testSteps = testSteps;
        this.pickleEvent = pickleEvent;
        this.dryRun = dryRun;
    }

    /**
     * Executes the test case.
     *
     * @param bus to which events should be broadcast
     * @deprecated not part of the public api
     */
    @Deprecated
    public void run(EventBus bus) {
        TestCase.SkipStatus skipNextStep = this.dryRun ? SkipStatus.SKIP_ALL_SKIPABLE : SkipStatus.RUN_ALL;
        Long startTime = bus.getTime();
        bus.send(new TestCaseStarted(startTime, this));
        ScenarioImpl scenarioResult = new ScenarioImpl(bus, pickleEvent);
        boolean finishingGherkinStep = false;
        for (TestStep step : testSteps) {
            if (skipNextStep == SkipStatus.RUN_HOOKS && finishingGherkinStep && step.startingGherkinStepType()) {
                skipNextStep = SkipStatus.SKIP_ALL_SKIPABLE;
            }
            Result stepResult = step.run(bus, pickleEvent.pickle.getLanguage(), scenarioResult, skipNextStep);
            if (!stepResult.is(Result.Type.PASSED) && skipNextStep == SkipStatus.RUN_ALL) {
                skipNextStep = SkipStatus.RUN_HOOKS;
            }
            scenarioResult.add(stepResult);
            finishingGherkinStep = step.finishingGherkinStepType();
        }
        Long stopTime = bus.getTime();
        bus.send(new TestCaseFinished(stopTime, this, new Result(scenarioResult.getStatus(), stopTime - startTime, scenarioResult.getError())));
    }

    public List<TestStep> getTestSteps() {
        return testSteps;
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
