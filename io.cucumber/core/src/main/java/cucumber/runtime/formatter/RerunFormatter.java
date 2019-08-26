package cucumber.runtime.formatter;

import gherkin.formatter.Formatter;
import gherkin.formatter.NiceAppendable;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.Background;
import gherkin.formatter.model.Examples;
import gherkin.formatter.model.Feature;
import gherkin.formatter.model.Match;
import gherkin.formatter.model.Result;
import gherkin.formatter.model.Scenario;
import gherkin.formatter.model.ScenarioOutline;
import gherkin.formatter.model.Step;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Formatter for reporting all failed features and print their locations
 * Failed means: (failed, undefined, pending) test result
 */
class RerunFormatter implements Formatter, Reporter, StrictAware {
    private final NiceAppendable out;
    private String featureLocation;
    private Scenario scenario;
    private boolean isTestFailed = false;
    private Map<String, ArrayList<Integer>> featureAndFailedLinesMapping = new HashMap<String, ArrayList<Integer>>();
    private boolean isStrict = false;

    public RerunFormatter(Appendable out) {
        this.out = new NiceAppendable(out);
    }

    @Override
    public void uri(String uri) {
        this.featureLocation = uri;
    }

    @Override
    public void feature(Feature feature) {
    }

    @Override
    public void background(Background background) {
    }

    @Override
    public void scenario(Scenario scenario) {
        this.scenario = scenario;
    }

    @Override
    public void scenarioOutline(ScenarioOutline scenarioOutline) {
    }

    @Override
    public void examples(Examples examples) {
    }

    @Override
    public void step(Step step) {
    }

    @Override
    public void eof() {
    }

    @Override
    public void syntaxError(String state, String event, List<String> legalEvents, String uri, Integer line) {
    }

    @Override
    public void done() {
        reportFailedScenarios();
    }

    private void reportFailedScenarios() {
        Set<Map.Entry<String, ArrayList<Integer>>> entries = featureAndFailedLinesMapping.entrySet();
        boolean firstFeature = true;
        for (Map.Entry<String, ArrayList<Integer>> entry : entries) {
            if (!entry.getValue().isEmpty()) {
                if (!firstFeature) {
                    out.append(" ");
                }
                out.append(entry.getKey());
                firstFeature = false;
                for (Integer line : entry.getValue()) {
                    out.append(":").append(line.toString());
                }
            }
        }
    }

    @Override
    public void close() {
        this.out.close();
    }

    @Override
    public void startOfScenarioLifeCycle(Scenario scenario) {
        isTestFailed = false;
    }

    @Override
    public void endOfScenarioLifeCycle(Scenario scenario) {
        if (isTestFailed) {
            recordTestFailed();
        }
    }

    @Override
    public void before(Match match, Result result) {
        if (isTestFailed(result)) {
            isTestFailed = true;
        }
    }

    @Override
    public void result(Result result) {
        if (isTestFailed(result)) {
            isTestFailed = true;
        }
    }

    private boolean isTestFailed(Result result) {
        String status = result.getStatus();
        return Result.FAILED.equals(status) || isStrict && (Result.UNDEFINED.getStatus().equals(status) || "pending".equals(status));
    }

    private void recordTestFailed() {
        ArrayList<Integer> failedScenarios = this.featureAndFailedLinesMapping.get(featureLocation);
        if (failedScenarios == null) {
            failedScenarios = new ArrayList<Integer>();
            this.featureAndFailedLinesMapping.put(featureLocation, failedScenarios);
        }

        failedScenarios.add(scenario.getLine());
    }

    @Override
    public void after(Match match, Result result) {
        if (isTestFailed(result)) {
            isTestFailed = true;
        }
    }

    @Override
    public void match(Match match) {
    }

    @Override
    public void embedding(String mimeType, byte[] data) {
    }

    @Override
    public void write(String text) {
    }

    @Override
    public void setStrict(boolean strict) {
        isStrict = strict;
    }
}
