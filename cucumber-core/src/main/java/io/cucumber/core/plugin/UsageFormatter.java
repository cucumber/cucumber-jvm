package io.cucumber.core.plugin;

import io.cucumber.messages.types.Envelope;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.Plugin;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.query.Query;
import io.cucumber.query.Repository;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import static io.cucumber.query.Repository.RepositoryFeature.INCLUDE_GHERKIN_DOCUMENTS;
import static io.cucumber.query.Repository.RepositoryFeature.INCLUDE_STEP_DEFINITIONS;
import static java.util.Objects.requireNonNull;

/**
 * Formatter to measure performance of steps. Includes average and median step
 * duration.
 */
public final class UsageFormatter implements Plugin, ConcurrentEventListener {

    private final MessagesToUsageWriter writer;

    @SuppressWarnings("WeakerAccess") // Used by PluginFactory
    public UsageFormatter(OutputStream out) {
        this.writer = MessagesToUsageWriter.builder(Jackson.OBJECT_MAPPER::writeValue)
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

    /**
     * Creates a usage report for step definitions based on a test run.
     * <p>
     * Note: Messages are first collected and only written once the stream is
     * closed.
     */
    public static final class MessagesToUsageWriter implements AutoCloseable {
    
        private final OutputStreamWriter out;
        private final Repository repository = Repository.builder()
                .feature(INCLUDE_GHERKIN_DOCUMENTS, true)
                .feature(INCLUDE_STEP_DEFINITIONS, true)
                .build();
        private final Query query = new Query(repository);
        private final Serializer serializer;
        private boolean streamClosed = false;
    
        public MessagesToUsageWriter(OutputStream out, Serializer serializer) {
            this.out = new OutputStreamWriter(
                requireNonNull(out),
                StandardCharsets.UTF_8);
            this.serializer = requireNonNull(serializer);
        }
    
        public void write(Envelope envelope) throws IOException {
            if (streamClosed) {
                throw new IOException("Stream closed");
            }
            repository.update(envelope);
        }
    
        public static Builder builder(Serializer serializer) {
            return new Builder(serializer);
        }
    
        public static final class Builder {
            private final Serializer serializer;
    
            private Builder(Serializer serializer) {
                this.serializer = requireNonNull(serializer);
            }
    
            public MessagesToUsageWriter build(OutputStream out) {
                requireNonNull(out);
                return new MessagesToUsageWriter(out, serializer);
            }
        }
    
        @Override
        public void close() throws IOException {
            if (streamClosed) {
                return;
            }
            try {
                UsageReportWriter.UsageReport report = new UsageReportWriter(query).createUsageReport();
                serializer.writeValue(out, report);
            } finally {
                try {
                    out.close();
                } finally {
                    streamClosed = true;
                }
            }
        }
    
        @FunctionalInterface
        public interface Serializer {
    
            void writeValue(Writer writer, Object value) throws IOException;
    
        }
    }
}
