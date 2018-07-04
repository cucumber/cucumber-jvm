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
import cucumber.runtime.UndefinedStepsTracker;
import cucumber.runtime.Utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

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

    private final UndefinedStepsTracker undefinedStepsTracker;


    /**
     * Creates a new instance for the given parameters
     *
     * @param instrumentation the {@link android.app.Instrumentation} to report statuses to
     */
    public AndroidInstrumentationReporter(final UndefinedStepsTracker undefinedStepsTracker, final Instrumentation instrumentation) {
        this.undefinedStepsTracker = undefinedStepsTracker;
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
        // Since the names of test cases are not guaranteed to be unique, we must check for unique names
        currentTestCaseName = calculateUniqueTestName(testCase);
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
        return undefinedStepsTracker.getSnippets().get(undefinedStepsTracker.getSnippets().size() - 1);
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

    /**
     * The stored unique test name for a test case.
     * We use an identity hash-map since we want to distinct all test case objects.
     * Thus, the key is a unique test case object.<br/>
     * The mapped value is the unique test name, which maybe differs from test case original non-unique name.
     */
    private final Map<TestCase, String> uniqueTestNameForTestCase = new IdentityHashMap<TestCase, String>();

    /**
     * The stored unique test names grouped by feature.<br/>
     * The key contains the feature file.<br/>
     * The mapped value is a set of unique test names for the feature.
     */
    private final Map<String, Set<String>> uniqueTestNamesForFeature = new HashMap<String, Set<String>>();

    /**
     * Creates a unique test name for the given test case by filling the internal maps
     * {@link #uniqueTestNameForTestCase} and {@link #uniqueTestNamesForFeature}.<br/>
     * If the test case name is unique, it will be used, otherwise, a index will be added " 2", " 3", " 4", ...
     * @param testCase the test case
     * @return a unique test name
     */
    private String calculateUniqueTestName(TestCase testCase) {
        String existingName = uniqueTestNameForTestCase.get(testCase);
        if (existingName != null) {
            // Nothing to do: there is already a test name for the passed test case object
            return existingName;
        }
        final String feature = testCase.getUri();
        String uniqueTestCaseName = testCase.getName();
        if (!uniqueTestNamesForFeature.containsKey(feature)) {
            // First test case of the feature
            uniqueTestNamesForFeature.put(feature, new HashSet<String>());
        }
        final Set<String> uniqueTestNamesSetForFeature = uniqueTestNamesForFeature.get(feature);
        // If "name" already exists, the next one is "name_2" or "name with spaces 2"
        int i = 2;
        while (uniqueTestNamesSetForFeature.contains(uniqueTestCaseName)) {
            uniqueTestCaseName = Utils.getUniqueTestNameForScenarioExample(testCase.getName(), i);
            i++;
        }
        uniqueTestNamesSetForFeature.add(uniqueTestCaseName);
        uniqueTestNameForTestCase.put(testCase, uniqueTestCaseName);
        return uniqueTestNameForTestCase.get(testCase);
    }

}
