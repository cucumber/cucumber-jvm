package cucumber.api.testng;

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
import org.testng.ITestResult;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.testng.Reporter.getCurrentTestResult;
import static org.testng.Reporter.log;

public class TestNgReporter implements Formatter, Reporter {

    private final NiceAppendable out;

    private AtomicInteger failureCount = new AtomicInteger(0);
    private AtomicInteger skipCount = new AtomicInteger(0);
    private AtomicInteger passCount = new AtomicInteger(0);

    private LinkedList<Step> steps = new LinkedList<Step>();

    public TestNgReporter(Appendable appendable) {
        out = new NiceAppendable(appendable);
    }

    @Override
    public void uri(String uri) {
        logDiv("Feature File: " + uri, "featureFile");
    }

    @Override
    public void feature(Feature feature) {
        logDiv("Feature: " + feature.getName(), "feature");
    }

    @Override
    public void background(Background background) {
    }

    @Override
    public void scenario(Scenario scenario) {
        logDiv("Scenario: " + scenario.getName(), "scenario");
    }

    @Override
    public void scenarioOutline(ScenarioOutline scenarioOutline) {
        logDiv("Scenario Outline: " + scenarioOutline.getName(), "scenarioOutline");
    }

    @Override
    public void examples(Examples examples) {
    }

    @Override
    public void step(Step step) {
        steps.add(step);
    }

    @Override
    public void eof() {
    }

    @Override
    public void syntaxError(String s, String s2, List<String> strings, String s3, Integer integer) {
    }

    @Override
    public void done() {
        steps.clear();
    }

    @Override
    public void close() {
        out.close();
    }

    @Override
    public void before(Match match, Result result) {
    }

    @Override
    public void result(Result result) {
        logResult(result);

        if (Result.FAILED.equals(result.getStatus())) {
            ITestResult tr = getCurrentTestResult();
            tr.setThrowable(result.getError());
            tr.setStatus(ITestResult.FAILURE);
            failureCount.incrementAndGet();
        } else if (Result.SKIPPED.equals(result)) {
            ITestResult tr = getCurrentTestResult();
            tr.setThrowable(result.getError());
            tr.setStatus(ITestResult.SKIP);
            skipCount.incrementAndGet();
        } else if (Result.UNDEFINED.equals(result)) {
            ITestResult tr = getCurrentTestResult();
            tr.setThrowable(result.getError());
            tr.setStatus(ITestResult.FAILURE);
            failureCount.incrementAndGet();
        } else {
            passCount.incrementAndGet();
        }
    }

    private void logResult(Result result) {
        String timing = computeTiming(result);

        Step step;
        if (steps.isEmpty()) {
            step = new Step(null, "MISMATCH BETWEEN STEPS AND RESULTS", "", 0, null, null);
        } else {
            step = steps.pop();
        }

        String format = "%s %s (%s%s)";
        String message = String.format(format, step.getKeyword(),
                step.getName(), result.getStatus(), timing);

        logDiv(message, "result");
    }

    private String computeTiming(Result result) {
        String timing = "";

        if (result.getDuration() != null) {
            // TODO: Get known about the magic nature number and get rid of it.
            int duration = Math.round(result.getDuration() / 1000000000);
            timing = " : " + duration + "s";
        }

        return timing;
    }

    @Override
    public void after(Match match, Result result) {
    }

    @Override
    public void write(String s) {
    }

    @Override
    public void match(Match match) {
    }

    @Override
    public void embedding(String s, byte[] bytes) {
    }

    private void logDiv(String message, String cssClassName) {
        String format = "<div \"%s\">%s</div>";
        String output = String.format(format, cssClassName, message);

        log(output);
    }

}
