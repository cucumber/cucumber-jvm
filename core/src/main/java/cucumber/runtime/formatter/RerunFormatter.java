package cucumber.runtime.formatter;

import gherkin.formatter.Formatter;
import gherkin.formatter.NiceAppendable;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.*;

import java.util.*;


/**
 * Formatter for reporting all failed features and print their locations
 * Failed means: (failed, undefined, pending) test result
 */
class RerunFormatter implements Formatter, Reporter {

    private final NiceAppendable out;

    private String featureLocation;

    private Step step;

    private Map<String, LinkedHashSet<Integer>> featureAndFailedLinesMapping = new HashMap<String, LinkedHashSet<Integer>>();


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
    }

    @Override
    public void scenarioOutline(ScenarioOutline scenarioOutline) {
    }

    @Override
    public void examples(Examples examples) {
    }

    @Override
    public void step(Step step) {
        this.step = step;
    }

    @Override
    public void eof() {
    }

    @Override
    public void syntaxError(String state, String event, List<String> legalEvents, String uri, Integer line) {
    }

    @Override
    public void done() {
        reportFailedSteps();
    }

    private void reportFailedSteps() {
        Set<Map.Entry<String, LinkedHashSet<Integer>>> entries = featureAndFailedLinesMapping.entrySet();
        boolean firstFeature = true;
        for (Map.Entry<String, LinkedHashSet<Integer>> entry : entries) {
            if (entry.getValue().size() > 0) {
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
    public void before(Match match, Result result) {

    }

    @Override
    public void result(Result result) {
        if (isTestFailed(result)) {
            recordTestFailed();
        }
    }

    private boolean isTestFailed(Result result) {
        String status = result.getStatus();
        return Result.FAILED.equals(status) || Result.UNDEFINED.getStatus().equals(status) || "pending".equals(status);
    }

    private void recordTestFailed() {
        LinkedHashSet<Integer> failedSteps = this.featureAndFailedLinesMapping.get(featureLocation);
        if (failedSteps == null) {
            failedSteps = new LinkedHashSet<Integer>();
            this.featureAndFailedLinesMapping.put(featureLocation, failedSteps);
        }

        failedSteps.add(step.getLine());
    }

    @Override
    public void after(Match match, Result result) {
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
}
