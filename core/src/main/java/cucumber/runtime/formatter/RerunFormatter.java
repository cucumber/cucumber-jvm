package cucumber.runtime.formatter;

import cucumber.api.TestCase;
import cucumber.api.event.EventHandler;
import cucumber.api.event.EventPublisher;
import cucumber.api.event.TestCaseFinished;
import cucumber.api.event.TestRunFinished;
import cucumber.api.formatter.Formatter;
import cucumber.api.formatter.NiceAppendable;
import cucumber.api.formatter.StrictAware;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * Formatter for reporting all failed test cases and print their locations
 * Failed means: results that make the exit code non-zero.
 */
class RerunFormatter implements Formatter, StrictAware {
    private final NiceAppendable out;
    private Map<String, ArrayList<Integer>> featureAndFailedLinesMapping = new HashMap<String, ArrayList<Integer>>();
    private boolean isStrict = false;

    private EventHandler<TestCaseFinished> testCaseFinishedHandler = new EventHandler<TestCaseFinished>() {
        @Override
        public void receive(TestCaseFinished event) {
            handeTestCaseFinished(event);
        }
    };
    private EventHandler<TestRunFinished> runFinishHandler = new EventHandler<TestRunFinished>() {
        @Override
        public void receive(TestRunFinished event) {
            handleTestRunFinished();
        }
    };

    public RerunFormatter(Appendable out) {
        this.out = new NiceAppendable(out);
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(TestCaseFinished.class, testCaseFinishedHandler);
        publisher.registerHandlerFor(TestRunFinished.class, runFinishHandler);
    }

    @Override
    public void setStrict(boolean strict) {
        isStrict = strict;
    }

    private void handeTestCaseFinished(TestCaseFinished event) {
        if (!event.result.isOk(isStrict)) {
            recordTestFailed(event.testCase);
        }
    }

    private void handleTestRunFinished() {
        reportFailedTestCases();
        out.close();
    }

    private void recordTestFailed(TestCase testCase) {
        String path = testCase.getUri();
        ArrayList<Integer> failedTestCases = this.featureAndFailedLinesMapping.get(path);
        if (failedTestCases == null) {
            failedTestCases = new ArrayList<Integer>();
            this.featureAndFailedLinesMapping.put(path, failedTestCases);
        }

        failedTestCases.add(testCase.getLine());
    }

    private void reportFailedTestCases() {
        Set<Map.Entry<String, ArrayList<Integer>>> entries = featureAndFailedLinesMapping.entrySet();
        for (Map.Entry<String, ArrayList<Integer>> entry : entries) {
            if (!entry.getValue().isEmpty()) {
                out.append(entry.getKey());
                for (Integer line : entry.getValue()) {
                    out.append(":").append(line.toString());
                }
                out.println();
            }
        }
    }
}
