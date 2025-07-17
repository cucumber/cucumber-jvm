package io.cucumber.core.plugin;

import io.cucumber.core.feature.FeatureWithLines;
import io.cucumber.messages.types.Envelope;
import io.cucumber.messages.types.Pickle;
import io.cucumber.messages.types.TestCase;
import io.cucumber.messages.types.TestCaseFinished;
import io.cucumber.messages.types.TestCaseStarted;
import io.cucumber.messages.types.TestRunFinished;
import io.cucumber.messages.types.TestStepFinished;
import io.cucumber.messages.types.TestStepResultStatus;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.EventPublisher;

import java.io.File;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static io.cucumber.core.feature.FeatureWithLines.create;
import static io.cucumber.messages.types.TestStepResultStatus.PASSED;
import static io.cucumber.messages.types.TestStepResultStatus.SKIPPED;
import static java.util.Collections.emptyList;
import static java.util.Comparator.comparing;
import static java.util.Objects.requireNonNull;

/**
 * Formatter for reporting all failed test cases and print their locations
 * Failed means: results that make the exit code non-zero.
 */
public final class RerunFormatter implements ConcurrentEventListener {

    private final Query query = new Query();
    private final Map<String, Set<Integer>> featureAndFailedLinesMapping = new HashMap<>();
    private final PrintWriter writer;

    public RerunFormatter(OutputStream out) {
        this.writer = createPrintWriter(out);
    }

    private static PrintWriter createPrintWriter(OutputStream out) {
        return new PrintWriter(
            new OutputStreamWriter(
                requireNonNull(out),
                StandardCharsets.UTF_8));
    }

    static URI relativize(URI uri) {
        if (!"file".equals(uri.getScheme())) {
            return uri;
        }
        if (!uri.isAbsolute()) {
            return uri;
        }

        try {
            URI root = new File("").toURI();
            URI relative = root.relativize(uri);
            // Scheme is lost by relativize
            return new URI("file", relative.getSchemeSpecificPart(), relative.getFragment());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(Envelope.class, event -> {
            query.update(event);
            event.getTestCaseFinished().ifPresent(this::handleTestCaseFinished);
            event.getTestRunFinished().ifPresent(this::handleTestRunFinished);
        });
    }


    private void handleTestCaseFinished(TestCaseFinished event) {
        TestStepResultStatus status = query.findMostSevereTestStepResultBy(event)
                // By definition
                .orElse(PASSED);
        if (status == PASSED || status == SKIPPED) {
            return;
        }
        query.findPickleBy(event).ifPresent(pickle -> {
            // Adds the entire feature for rerunning
            Set<Integer> lines = featureAndFailedLinesMapping.computeIfAbsent(pickle.getUri(), s -> new HashSet<>());
            pickle.getLocation().ifPresent(location -> {
                // Adds the specific scenarios
                // TODO: Messages are silly
                lines.add((int) (long) location.getLine());
            });
        });
    }

    private void handleTestRunFinished(TestRunFinished testRunFinished) {
        for (Map.Entry<String, Set<Integer>> entry : featureAndFailedLinesMapping.entrySet()) {
            String key = entry.getKey();
            // TODO: Should these be relative?
            FeatureWithLines featureWithLines = create(relativize(URI.create(key)), entry.getValue());
            writer.println(featureWithLines);
        }

        writer.close();
    }

    /**
     * Miniaturized version of Cucumber Query.
     * <p>
     * The rerun plugin only needs a few things.
     */
    private static class Query {

        private final Map<String, TestCase> testCaseById = new HashMap<>();
        private final Map<String, List<TestStepResultStatus>> testStepsResultStatusByTestCaseStartedId = new HashMap<>();
        private final Map<String, TestCaseStarted> testCaseStartedById = new HashMap<>();
        private final Map<String, Pickle> pickleById = new HashMap<>();

        void update(Envelope envelope) {
            envelope.getPickle().ifPresent(this::updatePickle);
            envelope.getTestCase().ifPresent(this::updateTestCase);
            envelope.getTestCaseStarted().ifPresent(this::updateTestCaseStarted);
            envelope.getTestStepFinished().ifPresent(this::updateTestStepFinished);
        }

        private void updatePickle(Pickle event) {
            pickleById.put(event.getId(), event);
        }

        private void updateTestCase(TestCase event) {
            testCaseById.put(event.getId(), event);
        }

        private void updateTestCaseStarted(TestCaseStarted testCaseStarted) {
            testCaseStartedById.put(testCaseStarted.getId(), testCaseStarted);
        }

        private void updateTestStepFinished(TestStepFinished event) {
            String testCaseStartedId = event.getTestCaseStartedId();
            testStepsResultStatusByTestCaseStartedId.computeIfAbsent(testCaseStartedId, s -> new ArrayList<>())
                    .add(event.getTestStepResult().getStatus());
        }

        public Optional<TestStepResultStatus> findMostSevereTestStepResultBy(TestCaseFinished testCaseFinished) {
            List<TestStepResultStatus> statuses = testStepsResultStatusByTestCaseStartedId
                    .getOrDefault(testCaseFinished.getTestCaseStartedId(), emptyList());
            if (statuses.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(Collections.max(statuses, comparing(Enum::ordinal)));
        }

        public Optional<Pickle> findPickleBy(TestCaseFinished testCaseFinished) {
            String testCaseStartedId = testCaseFinished.getTestCaseStartedId();
            TestCaseStarted testCaseStarted = testCaseStartedById.get(testCaseStartedId);
            if (testCaseStarted == null) {
                return Optional.empty();
            }
            TestCase testCase = testCaseById.get(testCaseStarted.getTestCaseId());
            if (testCase == null) {
                return Optional.empty();
            }
            return Optional.ofNullable(pickleById.get(testCase.getPickleId()));
        }

    }

}
