package cucumber.runtime.formatter;

import android.app.Instrumentation;
import android.os.Bundle;
import cucumber.api.Result;
import cucumber.api.TestCase;
import cucumber.api.event.EventHandler;
import cucumber.api.event.EventPublisher;
import cucumber.api.event.TestCaseFinished;
import cucumber.api.event.TestCaseStarted;
import cucumber.api.event.TestSourceRead;
import cucumber.api.event.TestStepFinished;
import cucumber.api.formatter.Formatter;
import cucumber.runtime.Runtime;

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
public final class AndroidInstrumentationReporter implements Formatter {

    /**
     * Tests status keys.
     */
    static class StatusKeys {
        static final String TEST = "test";
        static final String CLASS = "class";
        static final String STACK = "stack";
        static final String NUMTESTS = "numtests";
    }

    /**
     * Test result status codes.
     */
    static class StatusCodes {
        static final int FAILURE = -2;
        static final int START = 1;
        static final int ERROR = -1;
        static final int OK = 0;
    }

    /**
     * The collected TestSourceRead events.
     */
    private final TestSourcesModel testSources = new TestSourcesModel();

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
    private int numberOfTests;

    /**
     * The severest step result of the current test execution.
     * This might be a step or hook result.
     */
    private Result severestResult;

    /**
     * The uri of the feature file of the current test case.
     */
    private String currentUri;

    /**
     * The name of the current feature.
     */
    private String currentFeatureName;

    /**
     * The name of the current test case.
     */
    private String currentTestCaseName;

    /**
     * The event handler for the {@link TestSourceRead} events.
     */
    private final EventHandler<TestSourceRead> testSourceReadHandler = new EventHandler<TestSourceRead>() {
        @Override
        public void receive(TestSourceRead event) {
            testSourceRead(event);
        }
    };

    /**
     * The event handler for the {@link TestCaseStarted} events.
     */
    private final EventHandler<TestCaseStarted> testCaseStartedHandler = new EventHandler<TestCaseStarted>() {
        @Override
        public void receive(TestCaseStarted event) {
            startTestCase(event.testCase);
        }
    };

    /**
     * The event handler for the {@link TestStepFinished} events.
     */
    private final EventHandler<TestStepFinished> testStepFinishedHandler = new EventHandler<TestStepFinished>() {
        @Override
        public void receive(TestStepFinished event) {
            finishTestStep(event.result);
        }
    };

    /**
     * The event handler for the {@link TestCaseFinished} events.
     */
    private final EventHandler<TestCaseFinished> testCaseFinishedHandler = new EventHandler<TestCaseFinished>() {
        @Override
        public void receive(TestCaseFinished event) {
            finishTestCase();
        }
    };

    /**
     * Creates a new instance for the given parameters
     *
     * @param runtime         the {@link cucumber.runtime.Runtime} to use
     * @param instrumentation the {@link android.app.Instrumentation} to report statuses to
     */
    public AndroidInstrumentationReporter(final Runtime runtime, final Instrumentation instrumentation) {
        this.runtime = runtime;
        this.instrumentation = instrumentation;
    }

    @Override
    public void setEventPublisher(final EventPublisher publisher) {
        publisher.registerHandlerFor(TestSourceRead.class, testSourceReadHandler);
        publisher.registerHandlerFor(TestCaseStarted.class, testCaseStartedHandler);
        publisher.registerHandlerFor(TestCaseFinished.class, testCaseFinishedHandler);
        publisher.registerHandlerFor(TestStepFinished.class, testStepFinishedHandler);
    }

    public void setNumberOfTests(final int numberOfTests) {
        this.numberOfTests = numberOfTests;
    }

    void testSourceRead(final TestSourceRead event) {
        testSources.addTestSourceReadEvent(event.uri, event);
    }

    void startTestCase(final TestCase testCase) {
        if (!testCase.getUri().equals(currentUri)) {
            currentUri = testCase.getUri();
            currentFeatureName = testSources.getFeatureName(currentUri);
        }
        currentTestCaseName = testCase.getName();
        resetSeverestResult();
        final Bundle testStart = createBundle(currentFeatureName, currentTestCaseName);
        instrumentation.sendStatus(StatusCodes.START, testStart);
    }

    void finishTestStep(final Result result) {
        checkAndSetSeverestStepResult(result);
    }

    void finishTestCase() {
        final Bundle testResult = createBundle(currentFeatureName, currentTestCaseName);

        switch (severestResult.getStatus()) {
            case FAILED:
                if (severestResult.getError() instanceof AssertionError) {
                    testResult.putString(StatusKeys.STACK, severestResult.getErrorMessage());
                    instrumentation.sendStatus(StatusCodes.FAILURE, testResult);
                } else {
                    testResult.putString(StatusKeys.STACK, getStackTrace(severestResult.getError()));
                    instrumentation.sendStatus(StatusCodes.ERROR, testResult);
                }
                break;
            case AMBIGUOUS:
                testResult.putString(StatusKeys.STACK, getStackTrace(severestResult.getError()));
                instrumentation.sendStatus(StatusCodes.ERROR, testResult);
                break;
            case PENDING:
                testResult.putString(StatusKeys.STACK, severestResult.getErrorMessage());
                instrumentation.sendStatus(StatusCodes.ERROR, testResult);
                break;
            case PASSED:
            case SKIPPED:
                instrumentation.sendStatus(StatusCodes.OK, testResult);
                break;
            case UNDEFINED:
                testResult.putString(StatusKeys.STACK, getStackTrace(new MissingStepDefinitionError(getLastSnippet())));
                instrumentation.sendStatus(StatusCodes.ERROR, testResult);
                break;
            default:
                throw new IllegalStateException("Unexpected result status: " + severestResult.getStatus());
        }
    }

    /**
     * Creates a template bundle for reporting the start and end of a test.
     *
     * @param path         of the feature file of the current execution
     * @param testCaseName of the test case of the current execution
     * @return the new {@link Bundle}
     */
    private Bundle createBundle(final String path, final String testCaseName) {
        final Bundle bundle = new Bundle();
        bundle.putInt(StatusKeys.NUMTESTS, numberOfTests);
        bundle.putString(StatusKeys.CLASS, String.format("%s", path));
        bundle.putString(StatusKeys.TEST, String.format("%s", testCaseName));
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
     * Checks if the given {@code result} is more severe than the current {@code severestResult} and
     * updates the {@code severestResult} if that should be the case.
     *
     * @param result the {@link Result} to check
     */
    private void checkAndSetSeverestStepResult(final Result result) {
        final boolean firstResult = severestResult == null;
        if (firstResult) {
            severestResult = result;
            return;
        }

        final boolean currentIsPassed = severestResult.is(Result.Type.PASSED);
        final boolean nextIsNotPassed = !result.is(Result.Type.PASSED);
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
    private static String getStackTrace(final Throwable throwable) {
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(stringWriter, true);
        throwable.printStackTrace(printWriter);
        return stringWriter.getBuffer().toString();
    }
}
