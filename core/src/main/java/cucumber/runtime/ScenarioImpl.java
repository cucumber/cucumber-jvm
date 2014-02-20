package cucumber.runtime;

import cucumber.api.Scenario;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.Result;
import gherkin.formatter.model.Tag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;

public class ScenarioImpl implements Scenario {
    private static final List<String> SEVERITY = asList("passed", "skipped", "undefined", "pending", "failed");
    private final List<Result> stepResults = new ArrayList<Result>();
    private final Reporter reporter;
    private final Set<Tag> tags;
    private final String scenarioName;

    public ScenarioImpl(Reporter reporter, Set<Tag> tags, String scenarioName) {
        this.reporter = reporter;
        this.tags = tags;
        this.scenarioName = scenarioName;
    }

    void add(Result result) {
        stepResults.add(result);
    }

    @Override
    public Collection<String> getSourceTagNames() {
        Set<String> result = new HashSet<String>();
        for (Tag tag : tags) {
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
        reporter.embedding(mimeType, data);
    }

    @Override
    public void write(String text) {
        reporter.write(text);
    }

    @Override
    public String getName() {
        return scenarioName;
    }
}
