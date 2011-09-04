package cucumber.runtime;

import gherkin.formatter.model.Result;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

/**
 * The aggregated result of the steps in a Scenario.
 */
public class ScenarioResult {
    private static final List<String> SEVERITY = asList("passed", "undefined", "pending", "skipped", "failed");
    private final List<Result> stepResults = new ArrayList<Result>();

    void add(Result result) {
        stepResults.add(result);
    }

    /**
     * @return one of: "passed", "undefined", "pending", "skipped", "failed"
     */
    public String getStatus() {
        int pos = 0;
        for (Result stepResult : stepResults) {
            pos = Math.max(pos, SEVERITY.indexOf(stepResult.getStatus()));
        }
        return SEVERITY.get(pos);
    }

    /**
     * @return true if {@link #getStatus()} returns "failed"
     */
    public boolean isFailed() {
        return "failed".equals(getStatus());
    }
}
