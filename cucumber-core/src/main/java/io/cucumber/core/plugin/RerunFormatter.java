package io.cucumber.core.plugin;

import io.cucumber.core.feature.FeatureWithLines;
import io.cucumber.messages.types.Envelope;
import io.cucumber.messages.types.TestCaseFinished;
import io.cucumber.messages.types.TestStepResult;
import io.cucumber.messages.types.TestStepResultStatus;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.query.Query;
import io.cucumber.query.Repository;

import java.io.File;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static io.cucumber.core.feature.FeatureWithLines.create;
import static io.cucumber.query.Repository.RepositoryFeature.INCLUDE_GHERKIN_DOCUMENT;
import static java.util.Objects.requireNonNull;

/**
 * Formatter for reporting all failed test cases and print their locations
 * Failed means: results that make the exit code non-zero.
 */
public final class RerunFormatter implements ConcurrentEventListener {

    private final PrintWriter writer;
    private final Map<String, Set<Integer>> featureAndFailedLinesMapping = new LinkedHashMap<>();
    private final Repository repository = Repository.builder()
            .feature(INCLUDE_GHERKIN_DOCUMENT, true)
            .build();
    private final Query query = new Query(repository);

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
            repository.update(event);
            event.getTestCaseFinished().ifPresent(this::handleTestCaseFinished);
            event.getTestRunFinished().ifPresent(testRunFinished -> finishReport());
        });
    }

    private void handleTestCaseFinished(TestCaseFinished event) {
        TestStepResultStatus testStepResultStatus = query.findMostSevereTestStepResultBy(event)
                .map(TestStepResult::getStatus)
                // By definition
                .orElse(TestStepResultStatus.PASSED);

        if (testStepResultStatus == TestStepResultStatus.PASSED
                || testStepResultStatus == TestStepResultStatus.SKIPPED) {
            return;
        }

        query.findPickleBy(event).ifPresent(pickle -> {
            Set<Integer> lines = featureAndFailedLinesMapping
                    .computeIfAbsent(pickle.getUri(), s -> new HashSet<>());
            query.findLocationOf(pickle).ifPresent(location -> {
                // TODO: Messages are silly
                lines.add((int) (long) location.getLine());
            });
        });
    }

    private void finishReport() {
        for (Map.Entry<String, Set<Integer>> entry : featureAndFailedLinesMapping.entrySet()) {
            String key = entry.getKey();
            // TODO: Should these be relative?
            FeatureWithLines featureWithLines = create(relativize(URI.create(key)), entry.getValue());
            writer.println(featureWithLines);
        }

        writer.close();
    }

}
