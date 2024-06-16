package io.cucumber.core.plugin;


import io.cucumber.messages.types.Envelope;
import io.cucumber.query.Query;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Writes the message output of a test run as single json report.
 * <p>
 * Note: Messages are first collected and only written once the stream is closed.
 */
public class MessagesToJsonWriter implements AutoCloseable {

    private final OutputStreamWriter out;
    private final Query query = new Query();
    private final Serializer serializer;
    private boolean streamClosed = false;

    public MessagesToJsonWriter(OutputStream out, Serializer serializer) {
        this.out = new OutputStreamWriter(
                requireNonNull(out),
                StandardCharsets.UTF_8
        );
        this.serializer = serializer;
    }


    /**
     * Writes a cucumber message to the xml output.
     *
     * @param envelope the message
     * @throws IOException if an IO error occurs
     */
    public void write(Envelope envelope) throws IOException {
        if (streamClosed) {
            throw new IOException("Stream closed");
        }
        query.update(envelope);
    }

    /**
     * Closes the stream, flushing it first. Once closed further write()
     * invocations will cause an IOException to be thrown. Closing a closed
     * stream has no effect.
     *
     * @throws IOException if an IO error occurs
     */
    @Override
    public void close() throws IOException {
        if (streamClosed) {
            return;
        }
        try {
            List<Object> report = new JsonReportWriter(query).writeJsonReport();
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

