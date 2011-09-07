package cucumber.runtime;

import gherkin.formatter.model.Result;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

class ScenarioResultImpl implements ScenarioResult {
    private static final List<String> SEVERITY = asList("passed", "undefined", "pending", "skipped", "failed");
    private final List<Result> stepResults = new ArrayList<Result>();

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
}
