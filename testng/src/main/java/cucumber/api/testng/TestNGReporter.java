package cucumber.api.testng;

import static org.testng.Reporter.getCurrentTestResult;
import static org.testng.Reporter.log;

import cucumber.api.Result;
import cucumber.api.PickleStepTestStep;
import cucumber.api.event.EventHandler;
import cucumber.api.event.EventPublisher;
import cucumber.api.event.TestRunFinished;
import cucumber.api.event.TestStepFinished;
import cucumber.api.formatter.Formatter;
import cucumber.api.formatter.NiceAppendable;
import cucumber.runtime.Utils;
import org.testng.ITestResult;

class TestNGReporter implements Formatter {
    private final NiceAppendable out;
    private final EventHandler<TestStepFinished> testStepFinishedHandler = new EventHandler<TestStepFinished>() {
        @Override
        public void receive(TestStepFinished event) {
            if (event.testStep instanceof PickleStepTestStep) {
                PickleStepTestStep testStep = (PickleStepTestStep) event.testStep;
                result(testStep.getStepText(), event.result);
            }
        }
    };
    private EventHandler<TestRunFinished> runFinishHandler = new EventHandler<TestRunFinished>() {

        @Override
        public void receive(TestRunFinished event) {
            out.close();
        }
    };


    TestNGReporter(Appendable appendable) {
        out = new NiceAppendable(appendable);
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(TestStepFinished.class, testStepFinishedHandler);
        publisher.registerHandlerFor(TestRunFinished.class, runFinishHandler);
    }

    void uri(String uri) {
        // TODO: find an appropriate keyword
        String keyword = "Feature File";
        logDiv(keyword, uri, "featureFile");
    }

    private void result(String stepText, Result result) {
        logResult(stepText, result);
        ITestResult tr = getCurrentTestResult();

        switch (result.getStatus()) {
            case PASSED:
                // do nothing
                break;
            case FAILED:
            case AMBIGUOUS:
                tr.setThrowable(result.getError());
                tr.setStatus(ITestResult.FAILURE);
                break;
            case SKIPPED:
                tr.setThrowable(result.getError());
                tr.setStatus(ITestResult.SKIP);
                break;
            case UNDEFINED:
            case PENDING:
                tr.setThrowable(result.getError());
                tr.setStatus(ITestResult.FAILURE);
                break;
            default:
                throw new IllegalStateException("Unexpected result status: " + result.getStatus());
        }
    }

    private void logResult(String stepText, Result result) {
        String timing = computeTiming(result);

        String format = "%s (%s%s)";
        String message = String.format(format, stepText, result.getStatus(), timing);

        logDiv(message, "result");
    }

    private String computeTiming(Result result) {
        String timing = "";

        if (result.getDuration() != null) {
            // TODO: Get known about the magic nature number and get rid of it.
            int duration = Math.round(result.getDuration() / 1000000000);
            timing = " : " + duration + "s";
        }

        return timing;
    }

    private void logDiv(String message, String cssClassName) {
        String format = "<div \"%s\">%s</div>";
        String output = String.format(format, cssClassName, Utils.htmlEscape(message));
        log(output);
    }

    private void logDiv(String message, String message2, String cssClassName) {
        logDiv(message + ": " + message2, cssClassName);
    }

}
