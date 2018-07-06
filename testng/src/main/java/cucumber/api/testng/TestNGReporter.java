package cucumber.api.testng;

import cucumber.api.PickleStepTestStep;
import cucumber.api.Result;
import cucumber.api.event.ConcurrentEventListener;
import cucumber.api.event.EventHandler;
import cucumber.api.event.EventPublisher;
import cucumber.api.event.TestCaseFinished;
import cucumber.api.event.TestStepFinished;
import cucumber.api.formatter.StrictAware;
import cucumber.runtime.Utils;
import org.testng.ITestResult;
import org.testng.Reporter;

import java.util.concurrent.TimeUnit;

import static cucumber.api.Result.Type.SKIPPED;

class TestNGReporter implements ConcurrentEventListener, StrictAware {
    private boolean strict = false;
    private final EventHandler<TestStepFinished> testStepFinishedHandler = new EventHandler<TestStepFinished>() {
        @Override
        public void receive(TestStepFinished event) {
            if (event.testStep instanceof PickleStepTestStep) {
                PickleStepTestStep testStep = (PickleStepTestStep) event.testStep;
                handleTestStepFinished(testStep.getStepText(), event.result);
            }
        }
    };
    private final EventHandler<TestCaseFinished> testCaseFinishedHandler = new EventHandler<TestCaseFinished>() {
        @Override
        public void receive(TestCaseFinished event) {
            handleTestCaseFinished(event.result);
        }
    };

    TestNGReporter() {
    }

    @Override
    public void setStrict(boolean strict) {
        this.strict = strict;
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(TestStepFinished.class, testStepFinishedHandler);
        publisher.registerHandlerFor(TestCaseFinished.class, testCaseFinishedHandler);
    }

    private void handleTestStepFinished(String stepText, Result result) {
        String timing = computeTiming(result);

        String format = "%s (%s%s)";
        String message = String.format(format, stepText, result.getStatus(), timing);

        logDiv(message, "result");
    }

    private String computeTiming(Result result) {
        if (result.getDuration() == null) {
            return "";
        }
        return " : " + TimeUnit.NANOSECONDS.toSeconds(result.getDuration()) + "s";
    }

    private void logDiv(String message, String cssClassName) {
        String format = "<div \"%s\">%s</div>";
        String output = String.format(format, cssClassName, Utils.htmlEscape(message));
        Reporter.log(output);
    }

    private void handleTestCaseFinished(Result result) {
        ITestResult tr = Reporter.getCurrentTestResult();
        tr.setThrowable(result.getError());
        tr.setStatus(mapStatus(result));
    }

    private int mapStatus(Result status) {
        if (status.is(SKIPPED)) {
            return ITestResult.SKIP;
        }

        return status.isOk(strict) ? ITestResult.SUCCESS : ITestResult.FAILURE;
    }

}
