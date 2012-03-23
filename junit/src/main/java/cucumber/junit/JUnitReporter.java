package cucumber.junit;

import cucumber.runtime.PendingException;
import gherkin.formatter.Formatter;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.Background;
import gherkin.formatter.model.Examples;
import gherkin.formatter.model.Match;
import gherkin.formatter.model.Result;
import gherkin.formatter.model.Scenario;
import gherkin.formatter.model.ScenarioOutline;
import gherkin.formatter.model.Step;
import org.junit.internal.runners.model.EachTestNotifier;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

class JUnitReporter implements Reporter, Formatter {
    private final List<Step> steps = new ArrayList<Step>();

    private final Reporter reporter;
    private final Formatter formatter;

    private EachTestNotifier stepNotifier;
    private ExecutionUnitRunner executionUnitRunner;
    private RunNotifier runNotifier;
    private EachTestNotifier executionUnitNotifier;

    public JUnitReporter(Reporter reporter, Formatter formatter) {
        this.reporter = reporter;
        this.formatter = formatter;
    }

    public void startExecutionUnit(ExecutionUnitRunner executionUnitRunner, RunNotifier runNotifier) {
        this.executionUnitRunner = executionUnitRunner;
        this.runNotifier = runNotifier;
        this.stepNotifier = null;

        executionUnitNotifier = new EachTestNotifier(runNotifier, executionUnitRunner.getDescription());
        executionUnitNotifier.fireTestStarted();
    }


    public void finishExecutionUnit() {
        executionUnitNotifier.fireTestFinished();
    }

    public void match(Match match) {
        Description description = executionUnitRunner.describeChild(steps.remove(0));
        stepNotifier = new EachTestNotifier(runNotifier, description);
        reporter.match(match);
    }

    @Override
    public void embedding(String mimeType, InputStream data) {
        reporter.embedding(mimeType, data);
    }

    @Override
    public void write(String text) {
        reporter.write(text);
    }

    public void result(Result result) {
        Throwable error = result.getError();
        if (Result.SKIPPED == result || Result.UNDEFINED == result || error instanceof PendingException) {
            stepNotifier.fireTestIgnored();
        } else {
            if (stepNotifier != null) {
                //Should only fireTestStarted if not ignored
                stepNotifier.fireTestStarted();
                if (error != null) {
                    stepNotifier.addFailure(error);
                }
                stepNotifier.fireTestFinished();
            }
            if (error != null) {
                executionUnitNotifier.addFailure(error);
            }
        }
        if (steps.isEmpty()) {
            // We have run all of our steps. Set the stepNotifier to null so that
            // if an error occurs in an After block, it's reported against the scenario
            // instead (via executionUnitNotifier).
            stepNotifier = null;
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

    @Override
    public void done() {
        formatter.done();
    }

    @Override
    public void close() {
        formatter.close();
    }
}
