package cucumber.runtime;

import cucumber.api.Result;
import cucumber.api.Scenario;
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

import static java.util.Arrays.asList;

public class ScenarioImpl implements Scenario {
    private static final List<Result.Type> SEVERITY = asList(Result.Type.PASSED, Result.Type.SKIPPED, Result.Type.PENDING, Result.Type.UNDEFINED, Result.Type.AMBIGUOUS, Result.Type.FAILED);
    private final List<Result> stepResults = new ArrayList<Result>();
    private final List<PickleTag> tags;
    private final String uri;
    private final String scenarioName;
    private final String scenarioId;
    private final List<Integer> scenarioLines;
    private final EventBus bus;

    public ScenarioImpl(EventBus bus, PickleEvent pickleEvent) {
        this.bus = bus;
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
        int pos = 0;
        for (Result stepResult : stepResults) {
            pos = Math.max(pos, SEVERITY.indexOf(stepResult.getStatus()));
        }
        return SEVERITY.get(pos);
    }

    @Override
    public boolean isFailed() {
        return "failed".equals(getStatus());
    }

    @Override
    public void embed(byte[] data, String mimeType) {
        if (bus != null) {
            bus.send(new EmbedEvent(bus.getTime(), data, mimeType));
        }
    }

    @Override
    public void write(String text) {
        if (bus != null) {
            bus.send(new WriteEvent(bus.getTime(), text));
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
        Throwable error = null;
        int maxPos = 0;
        for (Result stepResult : stepResults) {
            int currentPos = SEVERITY.indexOf(stepResult.getStatus());
            if (currentPos > maxPos) {
                maxPos = currentPos;
                error = stepResult.getError();
            }
        }
        return error;
    }
}
