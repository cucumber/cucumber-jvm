package io.cucumber.core.plugin;

import io.cucumber.core.api.event.TestCase;
import io.cucumber.core.api.event.EventHandler;
import io.cucumber.core.api.event.EventListener;
import io.cucumber.core.api.event.EventPublisher;
import io.cucumber.core.api.event.TestCaseFinished;
import io.cucumber.core.api.event.TestRunFinished;
import io.cucumber.core.api.plugin.StrictAware;
import io.cucumber.core.feature.FeatureWithLines;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


/**
 * Formatter for reporting all failed test cases and print their locations
 * Failed means: results that make the exit code non-zero.
 */
public final class RerunFormatter implements EventListener, StrictAware {
    private final NiceAppendable out;
    private final Map<String, Collection<Integer>> featureAndFailedLinesMapping = new HashMap<>();
    private final EventHandler<TestRunFinished> runFinishHandler = new EventHandler<TestRunFinished>() {

        @Override
        public void receive(TestRunFinished event) {
            for (Map.Entry<String, Collection<Integer>> entry : featureAndFailedLinesMapping.entrySet()) {
                FeatureWithLines featureWithLines = FeatureWithLines.parse(entry.getKey(), entry.getValue());
                out.println(featureWithLines.toString());
            }

            out.close();
        }
    };
    private boolean isStrict = false;
    private final EventHandler<TestCaseFinished> testCaseFinishedHandler = new EventHandler<TestCaseFinished>() {

        @Override
        public void receive(TestCaseFinished event) {
            if (!event.result.isOk(isStrict)) {
                recordTestFailed(event.testCase);
            }
        }

        private void recordTestFailed(TestCase testCase) {
            String uri = testCase.getUri();
            Collection<Integer> failedTestCaseLines = getFailedTestCaseLines(uri);
            failedTestCaseLines.add(testCase.getLine());
        }

        private Collection<Integer> getFailedTestCaseLines(String uri) {
            Collection<Integer> failedTestCases = featureAndFailedLinesMapping.get(uri);
            if (failedTestCases == null) {
                failedTestCases = new ArrayList<>();
                featureAndFailedLinesMapping.put(uri, failedTestCases);
            }
            return failedTestCases;
        }
    };

    @SuppressWarnings("WeakerAccess") // Used by PluginFactory
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
}

