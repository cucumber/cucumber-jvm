package io.cucumber.core.plugin;

import io.cucumber.messages.types.Envelope;
import io.cucumber.messages.types.Location;
import io.cucumber.messages.types.Pickle;
import io.cucumber.messages.types.TestCaseStarted;
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
import java.util.Optional;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collector;

import static io.cucumber.query.Repository.RepositoryFeature.INCLUDE_GHERKIN_DOCUMENTS;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toCollection;

/**
 * Formatter for reporting all failed test cases and print their locations.
 */
public final class RerunFormatter implements ConcurrentEventListener {

    private final PrintWriter writer;
    private final Repository repository = Repository.builder()
            .feature(INCLUDE_GHERKIN_DOCUMENTS, true)
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
            event.getTestRunFinished().ifPresent(testRunFinished -> finishReport());
        });
    }

    private static final class UriAndLine {
        private final String uri;
        private final Long line;

        private UriAndLine(String uri, Long line) {
            this.uri = uri;
            this.line = line;
        }

        public String getUri() {
            return uri;
        }

        public Long getLine() {
            return line;
        }
    }

    private void finishReport() {
        query.findAllTestCaseStarted().stream()
                .filter(this::isNotPassingOrSkipped)
                .map(query::findPickleBy)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(this::createUriAndLine)
                .collect(groupByUriAndThenCollectLines())
                .forEach(this::printUriWithLines);
        writer.close();
    }

    private void printUriWithLines(String uri, TreeSet<Long> lines) {
        writer.println(renderFeatureWithLines(uri, lines));
    }

    private static Collector<UriAndLine, ?, TreeMap<String, TreeSet<Long>>> groupByUriAndThenCollectLines() {
        return groupingBy(
                UriAndLine::getUri,
                // Sort URIs
                TreeMap::new,
                mapping(
                        UriAndLine::getLine,
                        // Sort lines
                        toCollection(TreeSet::new)
                ));
    }

    private static StringBuilder renderFeatureWithLines(String uri, TreeSet<Long> lines) {
        String path = relativize(URI.create(uri)).toString();
        StringBuilder builder = new StringBuilder(path);
        for (Long line : lines) {
            builder.append(':');
            builder.append(line);
        }
        return builder;
    }

    private UriAndLine createUriAndLine(Pickle pickle) {
        String uri = pickle.getUri();
        Long line = query.findLocationOf(pickle).map(Location::getLine).orElse(null);
        return new UriAndLine(uri, line);
    }

    private boolean isNotPassingOrSkipped(TestCaseStarted event) {
        return query.findMostSevereTestStepResultBy(event)
                .map(TestStepResult::getStatus)
                .filter(status -> status != TestStepResultStatus.PASSED)
                .filter(status -> status != TestStepResultStatus.SKIPPED)
                .isPresent();
    }

}
