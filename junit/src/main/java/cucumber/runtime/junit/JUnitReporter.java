package cucumber.runtime.junit;

import static cucumber.runtime.Runtime.isPending;
import gherkin.formatter.Formatter;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.Background;
import gherkin.formatter.model.Examples;
import gherkin.formatter.model.Match;
import gherkin.formatter.model.Result;
import gherkin.formatter.model.Scenario;
import gherkin.formatter.model.ScenarioOutline;
import gherkin.formatter.model.Step;

import java.util.ArrayList;
import java.util.List;

import org.junit.internal.runners.model.EachTestNotifier;
import org.junit.runner.notification.RunNotifier;

import cucumber.api.PendingException;

public class JUnitReporter implements Reporter, Formatter {
    private final List<Step> steps = new ArrayList<Step>();

    private final Reporter reporter;
    private final Formatter formatter;
    private final boolean strict;

    private EachTestNotifier notifier;

    public JUnitReporter(Reporter reporter, Formatter formatter, boolean strict) {
        this.reporter = reporter;
        this.formatter = formatter;
        this.strict = strict;
    }

    public void startExecutionUnit(ExecutionUnitRunner executionUnitRunner, RunNotifier runNotifier) {
        notifier = strict ? 
            new EachTestNotifier(runNotifier, executionUnitRunner.getDescription()) : 
            new ExecutionUnitNotifier(runNotifier, executionUnitRunner.getDescription());
        notifier.fireTestStarted();
    }

    public void finishExecutionUnit() {
        notifier.fireTestFinished();
    }

    public void match(Match match) {
        reporter.match(match);
    }

    @Override
    public void embedding(String mimeType, byte[] data) {
        reporter.embedding(mimeType, data);
    }

    @Override
    public void write(String text) {
        reporter.write(text);
    }

    public void result(Result result) {
        Throwable error = result.getError();
        if (isPendingOrUndefined(result)) {
            addFailureOrIgnoreStep(result);
        } else if (error != null) {
            notifier.addFailure(error);
        }
        reporter.result(result);
    }

    private boolean isPendingOrUndefined(Result result) {
        return Result.UNDEFINED == result || isPending(result.getError());
    }

    private void addFailureOrIgnoreStep(Result result) {
        if (strict) {
            addFailure(result);
        } else {
            notifier.fireTestIgnored();
        }
    }

    private void addFailure(Result result) {
        Throwable error = result.getError();
        if (error == null) {
            error = new PendingException();
        }
        notifier.addFailure(error);
    }

    @Override
    public void before(Match match, Result result) {
        handleHook(result);
        reporter.before(match, result);
    }

    @Override
    public void after(Match match, Result result) {
        handleHook(result);
        reporter.after(match, result);
    }

    private void handleHook(Result result) {
        if (result.getStatus().equals(Result.FAILED)) {
            notifier.addFailure(result.getError());
        }
    }

    @Override
    public void uri(String uri) {
        formatter.uri(uri);
    }

    @Override
    public void feature(gherkin.formatter.model.Feature feature) {
        formatter.feature(feature);
    }

    @Override
    public void background(Background background) {
        formatter.background(background);
    }

    @Override
    public void scenario(Scenario scenario) {
        formatter.scenario(scenario);
    }

    @Override
    public void scenarioOutline(ScenarioOutline scenarioOutline) {
        formatter.scenarioOutline(scenarioOutline);
    }

    @Override
    public void examples(Examples examples) {
        formatter.examples(examples);
    }

    @Override
    public void step(Step step) {
        steps.add(step);
        formatter.step(step);
    }

    @Override
    public void eof() {
        formatter.eof();
    }

    @Override
    public void syntaxError(String state, String event, List<String> legalEvents, String uri, Integer line) {
        formatter.syntaxError(state, event, legalEvents, uri, line);
    }

    @Override
    public void done() {
        formatter.done();
    }

    @Override
    public void close() {
        formatter.close();
    }
}
