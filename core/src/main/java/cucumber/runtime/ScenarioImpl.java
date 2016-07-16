package cucumber.runtime;

import cucumber.api.Result;
import cucumber.api.Scenario;
import cucumber.api.event.EmbedEvent;
import cucumber.api.event.WriteEvent;
import cucumber.runner.EventBus;
import gherkin.pickles.Pickle;
import gherkin.pickles.PickleTag;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;

public class ScenarioImpl implements Scenario {
    private static final List<String> SEVERITY = asList("passed", "skipped", "pending", "undefined", "failed");
    private final List<Result> stepResults = new ArrayList<Result>();
    private final List<PickleTag> tags;
    private final String scenarioName;
    private final EventBus bus;

    public ScenarioImpl(EventBus bus, Pickle gherkinScenario) {
        this.bus = bus;
        List<PickleTag> tags;
        try { // TODO: Fix when Gherkin provide a getter for the tags.
            Field f;
            f = gherkinScenario.getClass().getDeclaredField("tags");
            f.setAccessible(true);
            tags = (List<PickleTag>) f.get(gherkinScenario);
        } catch (Exception e) {
            tags = Collections.<PickleTag>emptyList();
        }
        this.tags = tags;
        this.scenarioName = gherkinScenario.getName();
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
    public String getStatus() {
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
            bus.send(new EmbedEvent(data, mimeType));
        }
    }

    @Override
    public void write(String text) {
        if (bus != null) {
            bus.send(new WriteEvent(text));
        }
    }

    @Override
    public String getName() {
        return scenarioName;
    }
}
