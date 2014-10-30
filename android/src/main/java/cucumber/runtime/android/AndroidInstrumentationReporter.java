package cucumber.runtime.android;

import android.app.Instrumentation;
import android.os.Bundle;
import cucumber.runtime.Runtime;
import gherkin.formatter.model.Feature;
import gherkin.formatter.model.Match;
import gherkin.formatter.model.Result;
import gherkin.formatter.model.Scenario;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Reports the test results to the instrumentation through {@link Instrumentation#sendStatus(int, Bundle)} calls.
 * A "test" represents the execution of a scenario or scenario example lifecycle, which includes the execution of
 * following cucumber elements:
 * <ul>
 *     <li>all before hooks</li>
 *     <li>all background steps</li>
 *     <li>all scenario / scenario example steps</li>
 *     <li>all after hooks</li>
 * </ul>
 *
 * Test reports:
 * <ul>
 *     <li>"OK", when all step results are either "PASSED" or "SKIPPED"</li>
 *     <li>"FAILURE", when any step result of the background or scenario was "FAILED"</li>
 *     <li>"ERROR", when any step of the background or scenario or any before or after
 *     hook threw an exception other than an {@link AssertionError}</li>
 * </ul>
 */
public class AndroidInstrumentationReporter extends NoOpFormattingReporter {

    /**
     * Tests status keys.
     */
    public static class StatusKeys {
        public static final String TEST = "test";
        public static final String CLASS = "class";
        public static final String STACK = "stack";
        public static final String NUMTESTS = "numtests";
    }

    /**
     * Test result status codes.
     */
    public static class StatusCodes {
        public static final int FAILURE = -2;
        public static final int START = 1;
        public static final int ERROR = -1;
        public static final int OK = 0;
    }

    /**
     * The current cucumber runtime.
     */
    private final Runtime runtime;

    /**
     * The instrumentation to report to.
     */
    private final Instrumentation instrumentation;

    /**
     * The total number of tests which will be executed.
     */
    private final int numberOfTests;

    /**
     * The severest step result of the current test execution.
     * This might be a step or hook result.
     */
    private Result severestResult;

    /**
     * The feature of the current test execution.
     */
    private Feature currentFeature;

    /**
     * Creates a new instance for the given parameters
     *
     * @param runtime the {@link cucumber.runtime.Runtime} to use
     * @param instrumentation the {@link android.app.Instrumentation} to report statuses to
     * @param numberOfTests the total number of tests to be executed, this is expected to include all scenario outline runs
     */
    public AndroidInstrumentationReporter(
            final Runtime runtime,
            final Instrumentation instrumentation,
            final int numberOfTests) {

        this.runtime = runtime;
        this.instrumentation = instrumentation;
        this.numberOfTests = numberOfTests;
    }

    @Override
    public void feature(final Feature feature) {
        currentFeature = feature;
    }

    @Override
    public void startOfScenarioLifeCycle(final Scenario scenario) {
        resetSeverestResult();
        final Bundle testStart = createBundle(currentFeature, scenario);
        instrumentation.sendStatus(StatusCodes.START, testStart);
    }

    @Override
    public void before(final Match match, final Result result) {
        checkAndSetSeverestStepResult(result);
    }

    @Override
    public void result(final Result result) {
        checkAndSetSeverestStepResult(result);
    }

    @Override
    public void after(final Match match, final Result result) {
        checkAndSetSeverestStepResult(result);
    }

    @Override
    public void endOfScenarioLifeCycle(final Scenario scenario) {

        final Bundle testResult = createBundle(currentFeature, scenario);

        if (severestResult.getStatus().equals(Result.FAILED)) {

            if (severestResult.getError() instanceof AssertionError) {
                testResult.putString(StatusKeys.STACK, severestResult.getErrorMessage());
                instrumentation.sendStatus(StatusCodes.FAILURE, testResult);
            } else {
                testResult.putString(StatusKeys.STACK, getStackTrace(severestResult.getError()));
                instrumentation.sendStatus(StatusCodes.ERROR, testResult);
            }
            return;
        }

        if (severestResult.getStatus().equals(Result.PASSED)) {
            instrumentation.sendStatus( StatusCodes.OK, testResult);
            return;
        }

        if (severestResult.getStatus().equals(Result.SKIPPED.getStatus())) {
            instrumentation.sendStatus(StatusCodes.OK, testResult);
            return;
        }

        if (severestResult.getStatus().equals(Result.UNDEFINED.getStatus())) {
            testResult.putString(StatusKeys.STACK, getStackTrace(new MissingStepDefinitionError(getLastSnippet())));
            instrumentation.sendStatus(StatusCodes.ERROR, testResult);
            return;
        }

        throw new IllegalStateException("Unexpected result status: " + severestResult.getStatus());
    }

    /**
     * Creates a template bundle for reporting the start and end of a test.
     *
     * @param feature the {@link Feature} of the current execution
     * @param scenario the {@link Scenario} of the current execution
     * @return the new {@link Bundle}
     */
    private Bundle createBundle(final Feature feature, final Scenario scenario) {
        final Bundle bundle = new Bundle();
        bundle.putInt(StatusKeys.NUMTESTS, numberOfTests);
        bundle.putString(StatusKeys.CLASS, String.format("%s %s", feature.getKeyword(), feature.getName()));
        bundle.putString(StatusKeys.TEST, String.format("%s %s", scenario.getKeyword(), scenario.getName()));
        return bundle;
    }

    /**
     * Determines the last snippet for a detected undefined step.
     *
     * @return string representation of the snippet
     */
    private String getLastSnippet() {
        return runtime.getSnippets().get(runtime.getSnippets().size() - 1);
    }

    /**
     * Resets the severest test result for the next scenario life cycle.
     */
    private void resetSeverestResult() {
        severestResult = null;
    }

    /**
     * Checks if the given {@code result} is more severe than the current {@code severestResult} and updates
     * the {@code severestResult} if that should be the case.
     *
     * @param result the {@link Result} to check
     */
    private void checkAndSetSeverestStepResult(final Result result) {
        final boolean firstResult = severestResult == null;
        if (firstResult) {
            severestResult = result;
            return;
        }

        final boolean currentIsPassed = severestResult.getStatus().equals(Result.PASSED);
        final boolean nextIsNotPassed = !result.getStatus().equals(Result.PASSED);
        if (currentIsPassed && nextIsNotPassed) {
            severestResult = result;
        }
    }

    /**
     * Creates a string representation of the given {@code throwable}'s stacktrace.
     *
     * @param throwable the {@link Throwable} to get the stacktrace from
     * @return the stacktrace as a string
     */
    private  static String getStackTrace(final Throwable throwable) {
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(stringWriter, true);
        throwable.printStackTrace(printWriter);
        return stringWriter.getBuffer().toString();
    }
}
