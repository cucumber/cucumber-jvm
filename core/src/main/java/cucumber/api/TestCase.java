package cucumber.api;

import cucumber.api.event.TestCaseFinished;
import cucumber.api.event.TestCaseStarted;
import cucumber.runner.EventBus;
import cucumber.runtime.ScenarioImpl;
import gherkin.pickles.Pickle;
import gherkin.pickles.PickleLocation;
import gherkin.pickles.PickleTag;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

public class TestCase {
    private final Pickle pickle;
    private final List<TestStep> testSteps;

    public TestCase(List<TestStep> testSteps, Pickle pickle) {
        this.testSteps = testSteps;
        this.pickle = pickle;
    }

    public void run(EventBus bus, String language) {
        boolean skipNextStep = false;
        bus.send(new TestCaseStarted(bus.getTime(), this));
        ScenarioImpl scenarioResult = new ScenarioImpl(bus, pickle);
        for (TestStep step : testSteps) {
            Result stepResult = step.run(bus, language, scenarioResult, skipNextStep);
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
        return pickle.getName();
    }

    public String getScenarioDesignation() {
        return fileColonLine(pickle.getLocations().get(0)) + " # " + getName();
    }

    public String getPath() {
        return pickle.getLocations().get(0).getPath();
    }

    public int getLine() {
        return pickle.getLocations().get(0).getLine();
    }

    private String fileColonLine(PickleLocation location) {
        return location.getPath() + ":" + Integer.toString(location.getLine());
    }

    public List<PickleTag> getTags() {
        List<PickleTag> tags;
        try { // TODO: Fix when Gherkin provide a getter for the tags.
            Field f;
            f = pickle.getClass().getDeclaredField("tags");
            f.setAccessible(true);
            tags = (List<PickleTag>) f.get(pickle);
        } catch (Exception e) {
            tags = Collections.<PickleTag>emptyList();
        }
        return tags;
    }
}
