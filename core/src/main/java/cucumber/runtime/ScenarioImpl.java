package cucumber.runtime;

import cucumber.api.Result;
import cucumber.api.Scenario;
import cucumber.api.TestCase;
import cucumber.api.event.EmbedEvent;
import cucumber.api.event.WriteEvent;
import cucumber.runner.EventBus;
import gherkin.events.PickleEvent;
import gherkin.pickles.Pickle;
import gherkin.pickles.PickleLocation;
import gherkin.pickles.PickleTag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Collections.max;

public class ScenarioImpl implements Scenario {

    private final List<Result> stepResults = new ArrayList<Result>();
    private final List<PickleTag> tags;
    private final String uri;
    private final String scenarioName;
    private final String scenarioId;
    private final List<Integer> scenarioLines;
    private final EventBus bus;
    private final TestCase testCase;

    public ScenarioImpl(EventBus bus, TestCase testCase, PickleEvent pickleEvent) {
        this.bus = bus;
        this.testCase = testCase;
        Pickle pickle = pickleEvent.pickle;
        this.tags = pickle.getTags();
        this.uri = pickleEvent.uri;
        this.scenarioName = pickle.getName();
        List<PickleLocation> locations = pickle.getLocations();
        this.scenarioId = pickleEvent.uri + ":" + Integer.toString(locations.get(0).getLine());
        ArrayList<Integer> lines = new ArrayList<Integer>();
        for (PickleLocation location : locations) {
            lines.add(location.getLine());
        }
        this.scenarioLines = Collections.unmodifiableList(lines);
    }

    public void add(Result result) {
        stepResults.add(result);
    }

    @Override
    public Collection<String> getSourceTagNames() {
        Set<String> result = new HashSet<String>();
        for (PickleTag tag : tags) {
            result.add(tag.getName());
        }
        // Has to be a List in order for JRuby to convert to Ruby Array.
        return new ArrayList<String>(result);
    }

    @Override
    public Result.Type getStatus() {
        if (stepResults.isEmpty()) {
            return Result.Type.UNDEFINED;
        }

        return max(stepResults, Result.SEVERITY).getStatus();
    }

    @Override
    public boolean isFailed() {
        return getStatus() == Result.Type.FAILED;
    }

    @Override
    public void embed(byte[] data, String mimeType) {
        if (bus != null) {
            bus.send(new EmbedEvent(bus.getTime(), testCase, data, mimeType));
        }
    }

    @Override
    public void write(String text) {
        if (bus != null) {
            bus.send(new WriteEvent(bus.getTime(), testCase, text));
        }
    }

    @Override
    public String getName() {
        return scenarioName;
    }

    @Override
    public String getId() {
        return scenarioId;
    }

    @Override
    public String getUri() {
        return uri;
    }

    @Override
    public List<Integer> getLines() {
        return scenarioLines;
    }

    public Throwable getError() {
        if (stepResults.isEmpty()) {
            return null;
        }

        return max(stepResults, Result.SEVERITY).getError();
    }
}
