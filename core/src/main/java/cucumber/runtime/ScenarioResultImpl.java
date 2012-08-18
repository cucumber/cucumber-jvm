package cucumber.runtime;

import gherkin.formatter.Reporter;
import gherkin.formatter.model.Result;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

class ScenarioResultImpl implements ScenarioResult {
    private static final List<String> SEVERITY = asList("passed", "undefined", "pending", "skipped", "failed");
    private final List<Result> stepResults = new ArrayList<Result>();
    private final Reporter reporter;

    public ScenarioResultImpl(Reporter reporter) {
        this.reporter = reporter;
    }

    void add(Result result) {
        stepResults.add(result);
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
}
