package cucumber.runtime.junit;

import cucumber.api.PendingException;
import cucumber.runtime.CucumberException;
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

import java.util.ArrayList;
import java.util.List;

import static cucumber.runtime.Runtime.isPending;

public class JUnitReporter implements Reporter, Formatter {
    private final List<Step> steps = new ArrayList<Step>();

    private final Reporter reporter;
    private final Formatter formatter;
    private final boolean strict;
    private final JUnitOptions junitOptions;

    EachTestNotifier stepNotifier;
    private ExecutionUnitRunner executionUnitRunner;
    private RunNotifier runNotifier;
    EachTestNotifier executionUnitNotifier;
    private boolean failedStep;
    private boolean ignoredStep;
    private boolean inScenarioLifeCycle;

    public JUnitReporter(Reporter reporter, Formatter formatter, boolean strict, JUnitOptions junitOption) {
        this.reporter = reporter;
        this.formatter = formatter;
        this.strict = strict;
        this.junitOptions = junitOption;
    }

    public void startExecutionUnit(ExecutionUnitRunner executionUnitRunner, RunNotifier runNotifier) {
        this.executionUnitRunner = executionUnitRunner;
        this.runNotifier = runNotifier;
        this.stepNotifier = null;
        this.failedStep = false;
        this.ignoredStep = false;

        executionUnitNotifier = new EachTestNotifier(runNotifier, executionUnitRunner.getDescription());
        executionUnitNotifier.fireTestStarted();
    }

    public void finishExecutionUnit() {
        if (ignoredStep && !failedStep) {
            executionUnitNotifier.fireTestIgnored();
        }
        executionUnitNotifier.fireTestFinished();
    }

    public void match(Match match) {
        Step runnerStep = fetchAndCheckRunnerStep();
        Description description = executionUnitRunner.describeChild(runnerStep);
        stepNotifier = new EachTestNotifier(runNotifier, description);
        reporter.match(match);
        if (junitOptions.allowStartedIgnored()) {
            stepNotifier.fireTestStarted();
        }
    }

    private Step fetchAndCheckRunnerStep() {
        Step scenarioStep = steps.remove(0);
        Step runnerStep = executionUnitRunner.getRunnerSteps().remove(0);
        if (!scenarioStep.getName().equals(runnerStep.getName())) {
            throw new CucumberException("Expected step: \"" + scenarioStep.getName() + "\" got step: \"" + runnerStep.getName() + "\"");
        }
        return runnerStep;
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
        if (Result.SKIPPED == result) {
            stepNotifier.fireTestIgnored();
        } else if (isPendingOrUndefined(result)) {
            addFailureOrIgnoreStep(result);
        } else {
            if (stepNotifier != null) {
                //Should only fireTestStarted if not ignored
                if (!junitOptions.allowStartedIgnored()) {
                    stepNotifier.fireTestStarted();
                }
                if (error != null) {
                    stepNotifier.addFailure(error);
                }
                stepNotifier.fireTestFinished();
            }
            if (error != null) {
                failedStep = true;
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

    public boolean useFilenameCompatibleNames() {
        return junitOptions.filenameCompatibleNames();
    }

    private boolean isPendingOrUndefined(Result result) {
        Throwable error = result.getError();
        return Result.UNDEFINED == result || isPending(error);
    }

    private void addFailureOrIgnoreStep(Result result) {
        if (strict) {
            if (!junitOptions.allowStartedIgnored()) {
                stepNotifier.fireTestStarted();
            }
            addFailure(result);
            stepNotifier.fireTestFinished();
        } else {
            ignoredStep = true;
            stepNotifier.fireTestIgnored();
        }
    }

    private void addFailure(Result result) {

        Throwable error = result.getError();
        if (error == null) {
            error = new PendingException();
        }
        failedStep = true;
        stepNotifier.addFailure(error);
        executionUnitNotifier.addFailure(error);
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
        if (result.getStatus().equals(Result.FAILED) || (strict && isPending(result.getError()))) {
            executionUnitNotifier.addFailure(result.getError());
        } else if (isPending(result.getError())) {
            ignoredStep = true;
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
        if (inScenarioLifeCycle) {
            steps.add(step);
        }
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

    @Override
    public void startOfScenarioLifeCycle(Scenario scenario) {
        inScenarioLifeCycle = true;
        formatter.startOfScenarioLifeCycle(scenario);
    }

    @Override
    public void endOfScenarioLifeCycle(Scenario scenario) {
        formatter.endOfScenarioLifeCycle(scenario);
        inScenarioLifeCycle = false;
    }
}
