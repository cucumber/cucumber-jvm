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

    public TestCase(List<TestStep> testSteps, PickleEvent pickleEvent) {
        this.testSteps = testSteps;
        this.pickleEvent = pickleEvent;
    }

    public void run(EventBus bus) {
        boolean skipNextStep = false;
        bus.send(new TestCaseStarted(bus.getTime(), this));
        ScenarioImpl scenarioResult = new ScenarioImpl(bus, pickleEvent.pickle);
        for (TestStep step : testSteps) {
            Result stepResult = step.run(bus, pickleEvent.pickle.getLanguage(), scenarioResult, skipNextStep);
            if (stepResult.getStatus() != Result.PASSED) {
                skipNextStep = true;
            }
            scenarioResult.add(stepResult);
        }
        bus.send(new TestCaseFinished(bus.getTime(), this, new Result(scenarioResult.getStatus(), null, null)));
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

    public String getPath() {
        return pickleEvent.uri;
    }

    public int getLine() {
        return pickleEvent.pickle.getLocations().get(0).getLine();
    }

    private String fileColonLine(PickleLocation location) {
        return pickleEvent.uri + ":" + Integer.toString(location.getLine());
    }

    public List<PickleTag> getTags() {
        return pickleEvent.pickle.getTags();
    }
}
