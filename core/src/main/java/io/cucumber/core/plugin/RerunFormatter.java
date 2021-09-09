package io.cucumber.core.plugin;

import io.cucumber.core.feature.FeatureWithLines;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.TestCase;
import io.cucumber.plugin.event.TestCaseFinished;
import io.cucumber.plugin.event.TestRunFinished;

import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static io.cucumber.core.feature.FeatureWithLines.create;
import static io.cucumber.core.plugin.PrettyFormatter.relativize;

/**
 * Formatter for reporting all failed test cases and print their locations
 * Failed means: results that make the exit code non-zero.
 */
public final class RerunFormatter implements ConcurrentEventListener {

    private final NiceAppendable out;
    private final Map<URI, Collection<Integer>> featureAndFailedLinesMapping = new HashMap<>();

    public RerunFormatter(OutputStream out) {
        this.out = new NiceAppendable(new UTF8OutputStreamWriter(out));
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(TestCaseFinished.class, this::handleTestCaseFinished);
        publisher.registerHandlerFor(TestRunFinished.class, event -> finishReport());
    }

    private void handleTestCaseFinished(TestCaseFinished event) {
        if (!event.getResult().getStatus().isOk()) {
            recordTestFailed(event.getTestCase());
        }
    }

    private void finishReport() {
        for (Map.Entry<URI, Collection<Integer>> entry : featureAndFailedLinesMapping.entrySet()) {
            FeatureWithLines featureWithLines = create(relativize(entry.getKey()), entry.getValue());
            out.println(featureWithLines.toString());
        }

        out.close();
    }

    private void recordTestFailed(TestCase testCase) {
        URI uri = testCase.getUri();
        Collection<Integer> failedTestCaseLines = getFailedTestCaseLines(uri);
        failedTestCaseLines.add(testCase.getLocation().getLine());
    }

    private Collection<Integer> getFailedTestCaseLines(URI uri) {
        return featureAndFailedLinesMapping.computeIfAbsent(uri, k -> new ArrayList<>());
    }

}
