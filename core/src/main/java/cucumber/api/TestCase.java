package cucumber.api;

import java.util.List;

import cucumber.api.event.TestCaseFinished;
import cucumber.api.event.TestCaseStarted;
import cucumber.runner.EventBus;
import cucumber.runtime.ScenarioImpl;
import gherkin.events.PickleEvent;
import gherkin.pickles.PickleLocation;
import gherkin.pickles.PickleTag;

public class TestCase {
    private final PickleEvent pickleEvent;
    private final List<TestStep> testSteps;
    private final boolean dryRun;
    private int rerun_count = 1;
    private String testCaseId = "";

    /**
     * Creates a new instance of a test case.
     *
     * @param testSteps   of the test case
     * @param pickleEvent the pickle executed by this test case
     * @deprecated not part of the public api
     */
    @Deprecated
    public TestCase(List<TestStep> testSteps, PickleEvent pickleEvent) {
        this(testSteps, pickleEvent, false, 1);
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
    public TestCase(List<TestStep> testSteps, PickleEvent pickleEvent, boolean dryRun, int rerun_count) {
        this.testSteps = testSteps;
        this.pickleEvent = pickleEvent;
        this.dryRun = dryRun;
        this.rerun_count = rerun_count;
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
        int counter = 0;
        while (counter < rerun_count && (scenarioResult.getStatus() != Result.Type.PASSED ||
            scenarioResult.getStatus() == Result.Type.UNDEFINED)) {
            if (!this.dryRun) {
                skipNextStep = false;
            }
            scenarioResult = new ScenarioImpl(bus, pickleEvent);
            scenarioResult.setScenarioId(this.testCaseId);
            boolean reRunTest = true;
            for (TestStep step : testSteps) {
                Result stepResult = step.run(bus, pickleEvent.pickle.getLanguage(), scenarioResult, skipNextStep, reRunTest);
                if (!stepResult.is(Result.Type.PASSED)) {
                    skipNextStep = true;
                }
                scenarioResult.add(stepResult);
                reRunTest = false;
            }
            counter++;
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


    public String getTestCaseId() {
        return testCaseId;
    }

    public void setTestCaseId(String testCaseId) {
        this.testCaseId = testCaseId;
    }
}
