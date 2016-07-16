package cucumber.runtime.android;

import android.util.Log;
import cucumber.api.event.EventHandler;
import cucumber.api.event.EventPublisher;
import cucumber.api.event.TestCaseStarted;
import cucumber.api.event.TestRunFinished;
import cucumber.api.event.TestStepStarted;
import cucumber.api.formatter.Formatter;
import cucumber.runtime.Runtime;

/**
 * Logs information about the currently executed statements to androids logcat.
 */
public class  AndroidLogcatReporter implements Formatter {

    /**
     * The {@link cucumber.runtime.Runtime} to get the errors and snippets from for writing them to the logcat at the end of the execution.
     */
    private final Runtime runtime;

    /**
     * The log tag to be used when logging to logcat.
     */
    private final String logTag;

    /**
     * The event handler that logs the {@link TestCaseStarted} events.
     */
    private final EventHandler<TestCaseStarted> testCaseStartedHandler = new EventHandler<TestCaseStarted>() {
        @Override
        public void receive(TestCaseStarted event) {
            Log.d(logTag, String.format("%s", event.testCase.getName()));
        }
    };

    /**
     * The event handler that logs the {@link TestStepStarted} events.
     */
    private final EventHandler<TestStepStarted> testStepStartedHandler = new EventHandler<TestStepStarted>() {
        @Override
        public void receive(TestStepStarted event) {
            if (!event.testStep.isHook()) {
                Log.d(logTag, String.format("%s", event.testStep.getStepText()));
            }
        }
    };

    /**
     * The event handler that logs the {@link TestRunFinished} events.
     */
    private EventHandler<TestRunFinished> runFinishHandler = new EventHandler<TestRunFinished>() {

        @Override
        public void receive(TestRunFinished event) {
            for (final Throwable throwable : runtime.getErrors()) {
                Log.e(logTag, throwable.toString());
            }

            for (final String snippet : runtime.getSnippets()) {
                Log.w(logTag, snippet);
            }
        }
    };

    /**
     * Creates a new instance for the given parameters.
     *
     * @param runtime the {@link cucumber.runtime.Runtime} to get the errors and snippets from
     * @param logTag the tag to use for logging to logcat
     */
    public AndroidLogcatReporter(final Runtime runtime, final String logTag) {
        this.runtime = runtime;
        this.logTag = logTag;
    }

    @Override
    public void setEventPublisher(final EventPublisher publisher) {
        publisher.registerHandlerFor(TestCaseStarted.class, testCaseStartedHandler);
        publisher.registerHandlerFor(TestStepStarted.class, testStepStartedHandler);
        publisher.registerHandlerFor(TestRunFinished.class, runFinishHandler);
    }
}
