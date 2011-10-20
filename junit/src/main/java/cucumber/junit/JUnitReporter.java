package cucumber.junit;

import cucumber.runtime.PendingException;
import gherkin.formatter.Formatter;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.*;
import org.junit.internal.runners.model.EachTestNotifier;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;

import java.util.ArrayList;
import java.util.List;

class JUnitReporter implements Reporter, Formatter {
    private final List<Step> steps = new ArrayList<Step>();

    private final Reporter reporter;
    private final Formatter formatter;

    private EachTestNotifier eachTestNotifier;
    private Match match;
    private ExecutionUnitRunner executionUnitRunner;
    private RunNotifier notifier;

    public JUnitReporter(Reporter reporter, Formatter formatter) {
        this.reporter = reporter;
        this.formatter = formatter;
    }

    public void match(Match match) {
        this.match = match;

        Description description = executionUnitRunner.describeChild(steps.remove(0));
        eachTestNotifier = new EachTestNotifier(notifier, description);

        if (match == Match.UNDEFINED) {
            eachTestNotifier.fireTestIgnored();
        } else {
            eachTestNotifier.fireTestStarted();
        }
        reporter.match(match);
    }

    public void embedding(String mimeType, byte[] data) {
        reporter.embedding(mimeType, data);
    }

    public void result(Result result) {
        Throwable error = result.getError();
        if (Result.SKIPPED == result || error instanceof PendingException) {
            if (match != Match.UNDEFINED) {
                // No need to say it's ignored twice
                eachTestNotifier.fireTestIgnored();
            }
        } else {
            if (error != null) {
                eachTestNotifier.addFailure(error);
            }
            eachTestNotifier.fireTestFinished();
        }
        reporter.result(result);
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
    public void syntaxError(String state, String event, List<String> legalEvents, String uri, int line) {
        formatter.syntaxError(state, event, legalEvents, uri, line);
    }

    public void setStepParentRunner(ExecutionUnitRunner executionUnitRunner, RunNotifier notifier) {
        this.executionUnitRunner = executionUnitRunner;
        this.notifier = notifier;
    }

    public Formatter getFormatter() {
        return formatter;
    }

    public Reporter getReporter() {
        return reporter;
    }
}
