package io.cucumber.core.plugin;

import io.cucumber.messages.types.Envelope;
import io.cucumber.plugin.ColorAware;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.query.Query;
import io.cucumber.query.Repository;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.cucumber.query.Repository.RepositoryFeature.INCLUDE_GHERKIN_DOCUMENTS;
import static io.cucumber.query.Repository.RepositoryFeature.INCLUDE_STEP_DEFINITIONS;
import static java.util.Objects.requireNonNull;

/**
 * Formatter to measure performance of steps. Includes average and median step
 * duration.
 */
public final class UnusedStepsSummaryPrinter implements ColorAware, ConcurrentEventListener {

    private final MessagesToUnusedWriter writer;

    @SuppressWarnings("WeakerAccess") // Used by PluginFactory
    public UnusedStepsSummaryPrinter(OutputStream out) {
        String cwdUri = new File("").toPath().toUri().toString();
        this.writer = MessagesToUnusedWriter.builder()
                .removeUriPrefix(cwdUri)
                .build(out);
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(Envelope.class, this::write);
    }

    private void write(Envelope event) {
        try {
            writer.write(event);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        // TODO: Plugins should implement the closable interface
        // and be closed by Cucumber
        if (event.getTestRunFinished().isPresent()) {
            try {
                writer.close();
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    @Override
    public void setMonochrome(boolean monochrome) {
        // no-op, no colors printed
    }

    /**
     * Creates a usage report for step definitions based on a test run.
     * <p>
     * Note: Messages are first collected and only written once the stream is
     * closed.
     */
    static final class MessagesToUnusedWriter implements AutoCloseable {

        private final OutputStreamWriter out;
        private final Repository repository = Repository.builder()
                .feature(INCLUDE_GHERKIN_DOCUMENTS, true)
                .feature(INCLUDE_STEP_DEFINITIONS, true)
                .build();
        private final Query query = new Query(repository);
        private final Function<String, String> uriFormatter;
        private boolean streamClosed = false;

        public MessagesToUnusedWriter(OutputStream out, Function<String, String> uriFormatter) {
            this.out = new OutputStreamWriter(
                requireNonNull(out),
                StandardCharsets.UTF_8);
            this.uriFormatter = requireNonNull(uriFormatter);
        }

        public void write(Envelope envelope) throws IOException {
            if (streamClosed) {
                throw new IOException("Stream closed");
            }
            repository.update(envelope);
        }

        static Builder builder() {
            return new Builder();
        }

        static final class Builder {
            private Function<String, String> uriFormatter = Function.identity();

            private static Function<String, String> removePrefix(String prefix) {
                // TODO: Needs coverage
                return s -> {
                    if (s.startsWith(prefix)) {
                        return s.substring(prefix.length());
                    }
                    return s;
                };
            }

            /**
             * Removes a given prefix from all URI locations.
             * <p>
             * The typical usage would be to trim the current working directory.
             * This makes the report more readable.
             */
            public Builder removeUriPrefix(String prefix) {
                // TODO: Needs coverage
                this.uriFormatter = removePrefix(requireNonNull(prefix));
                return this;
            }

            public MessagesToUnusedWriter build(OutputStream out) {
                requireNonNull(out);
                return new MessagesToUnusedWriter(out, uriFormatter);
            }
        }

        @Override
        public void close() throws IOException {
            if (streamClosed) {
                return;
            }
            try {
                UsageReport report = new UsageReportBuilder(query, uriFormatter).build();
                List<UsageReport.StepDefinitionUsage> stepDefinitions = report.getStepDefinitions();
                List<UsageReport.StepDefinitionUsage> unusedSteps = stepDefinitions.stream()
                        .filter(stepDefinitionUsage -> stepDefinitionUsage.getSteps().isEmpty())
                        .collect(Collectors.toList());

                PrintWriter printWriter = new PrintWriter(out);
                printWriter.println(unusedSteps.size() + " Unused steps:");

                // Output results when done
                for (UsageReport.StepDefinitionUsage entry : unusedSteps) {
                    String location = entry.getLocation();
                    String pattern = entry.getExpression();
                    printWriter.println(location + " # " + pattern);
                }
            } finally {
                try {
                    out.close();
                } finally {
                    streamClosed = true;
                }
            }
        }

    }
}
