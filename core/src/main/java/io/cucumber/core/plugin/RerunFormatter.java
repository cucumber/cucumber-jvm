package io.cucumber.core.plugin;

import io.cucumber.core.feature.FeatureWithLines;
import io.cucumber.plugin.EventListener;
import io.cucumber.plugin.StrictAware;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.TestCase;
import io.cucumber.plugin.event.TestCaseFinished;
import io.cucumber.plugin.event.TestRunFinished;

import java.net.URI;
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
    private final Map<URI, Collection<Integer>> featureAndFailedLinesMapping = new HashMap<>();

    private boolean isStrict = false;

    @SuppressWarnings("WeakerAccess") // Used by PluginFactory
    public RerunFormatter(Appendable out) {
        this.out = new NiceAppendable(out);
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(TestCaseFinished.class, this::handleTestCaseFinished);
        publisher.registerHandlerFor(TestRunFinished.class, event -> finishReport());
    }

    @Override
    public void setStrict(boolean strict) {
        isStrict = strict;
    }

    private void handleTestCaseFinished(TestCaseFinished event) {
        if (!event.getResult().getStatus().isOk(isStrict)) {
            recordTestFailed(event.getTestCase());
        }
    }

    private void recordTestFailed(TestCase testCase) {
        URI uri = testCase.getUri();
        Collection<Integer> failedTestCaseLines = getFailedTestCaseLines(uri);
        failedTestCaseLines.add(testCase.getLine());
    }

    private Collection<Integer> getFailedTestCaseLines(URI uri) {
        return featureAndFailedLinesMapping.computeIfAbsent(uri, k -> new ArrayList<>());
    }

    private void finishReport() {
        for (Map.Entry<URI, Collection<Integer>> entry : featureAndFailedLinesMapping.entrySet()) {
            FeatureWithLines featureWithLines = FeatureWithLines.create(entry.getKey(), entry.getValue());
            out.println(featureWithLines.toString());
        }

        out.close();
    }
}

