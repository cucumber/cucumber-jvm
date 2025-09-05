package io.cucumber.core.plugin;

import io.cucumber.messages.types.Envelope;
import io.cucumber.messages.types.Location;
import io.cucumber.messages.types.Pickle;
import io.cucumber.messages.types.TestCaseFinished;
import io.cucumber.messages.types.TestRunFinished;
import io.cucumber.messages.types.TestStepResult;
import io.cucumber.messages.types.TestStepResultStatus;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.query.Query;

import java.io.File;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.TreeSet;

import static io.cucumber.messages.types.TestStepResultStatus.PASSED;
import static io.cucumber.messages.types.TestStepResultStatus.SKIPPED;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toCollection;

/**
 * Formatter for reporting all failed test cases and print their locations.
 */
public final class RerunFormatter implements ConcurrentEventListener {

    private final Query query = new Query();
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

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(Envelope.class, event -> {
            query.update(event);
            event.getTestRunFinished().ifPresent(this::handleTestRunFinished);
        });
    }

    private void handleTestRunFinished(TestRunFinished testRunFinished) {
        query.findAllTestCaseFinished()
                .stream()
                .filter(this::shouldBeRerun)
                .map(query::findPickleBy)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(this::createPickleWithLineEntry)
                // TreeSet makes the lines sorted and unique.
                .collect(groupingBy(Entry::getKey, mapping(Entry::getValue, toCollection(TreeSet::new))))
                .forEach((uri, lines) -> writer.println(renderFeatureWithLines(uri, lines)));
        writer.close();
    }

    private String renderFeatureWithLines(String feature, TreeSet<Long> lines) {
        URI uri = relativize(URI.create(feature));
        StringBuilder builder = new StringBuilder(uri.toString());
        for (Long line : lines) {
            builder.append(':');
            builder.append(line);
        }
        return builder.toString();
    }

    private Entry<String, Long> createPickleWithLineEntry(Pickle pickle) {
        String uri = pickle.getUri();
        Long line = query.findLocationOf(pickle)
                .map(Location::getLine)
                .orElse(null);
        return new SimpleEntry<>(uri, line);
    }

    private boolean shouldBeRerun(TestCaseFinished testCaseFinished) {
        TestStepResultStatus status = query.findMostSevereTestStepResultBy(testCaseFinished)
                .map(TestStepResult::getStatus)
                // By definition
                .orElse(PASSED);
        return !(status == PASSED || status == SKIPPED);
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
}
