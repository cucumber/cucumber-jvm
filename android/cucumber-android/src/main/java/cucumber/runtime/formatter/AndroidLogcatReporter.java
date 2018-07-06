package cucumber.runtime.formatter;

import android.util.Log;
import cucumber.api.PickleStepTestStep;
import cucumber.api.event.EventHandler;
import cucumber.api.event.EventPublisher;
import cucumber.api.event.TestCaseStarted;
import cucumber.api.event.TestRunFinished;
import cucumber.api.event.TestStepStarted;
import cucumber.api.formatter.Formatter;
import cucumber.runtime.UndefinedStepsTracker;

/**
 * Logs information about the currently executed statements to androids logcat.
 */
public final class AndroidLogcatReporter implements Formatter {

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

    private final Stats stats;

    private final UndefinedStepsTracker undefinedStepsTracker;

    /**
     * The event handler that logs the {@link TestStepStarted} events.
     */
    private final EventHandler<TestStepStarted> testStepStartedHandler = new EventHandler<TestStepStarted>() {
        @Override
        public void receive(TestStepStarted event) {
            if (event.testStep instanceof PickleStepTestStep) {
                PickleStepTestStep testStep = (PickleStepTestStep) event.testStep;
                Log.d(logTag, String.format("%s", testStep.getStepText()));
            }
        }
    };

    /**
     * The event handler that logs the {@link TestRunFinished} events.
     */
    private EventHandler<TestRunFinished> runFinishHandler = new EventHandler<TestRunFinished>() {

        @Override
        public void receive(TestRunFinished event) {
            for (final Throwable throwable : stats.getErrors()) {
                Log.e(logTag, throwable.toString());
            }

            for (final String snippet : undefinedStepsTracker.getSnippets()) {
                Log.w(logTag, snippet);
            }
        }
    };

    /**
     * Creates a new instance for the given parameters.
     *
     * @param undefinedStepsTracker
     * @param logTag the tag to use for logging to logcat
     */
    public AndroidLogcatReporter(Stats stats, UndefinedStepsTracker undefinedStepsTracker, final String logTag) {
        this.stats = stats;
        this.undefinedStepsTracker = undefinedStepsTracker;
        this.logTag = logTag;
    }

    @Override
    public void setEventPublisher(final EventPublisher publisher) {
        publisher.registerHandlerFor(TestCaseStarted.class, testCaseStartedHandler);
        publisher.registerHandlerFor(TestStepStarted.class, testStepStartedHandler);
        publisher.registerHandlerFor(TestRunFinished.class, runFinishHandler);
    }
}
