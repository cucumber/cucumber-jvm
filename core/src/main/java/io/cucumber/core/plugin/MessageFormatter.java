package io.cucumber.core.plugin;

import io.cucumber.messages.Messages.Envelope;
import io.cucumber.messages.internal.com.google.protobuf.util.JsonFormat;
import io.cucumber.plugin.EventListener;
import io.cucumber.plugin.event.EventPublisher;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

public final class MessageFormatter implements EventListener {
    private final OutputStream outputStream;
    private final Writer writer;
    private final JsonFormat.Printer jsonPrinter = JsonFormat.printer()
        .omittingInsignificantWhitespace();
    private final ProtobufFormat format;

    public MessageFormatter(OutputStream outputStream) {
        this.format = ProtobufFormat.NDJSON;
        this.outputStream = outputStream;
        this.writer = new OutputStreamWriter(this.outputStream, StandardCharsets.UTF_8);
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(Envelope.class, this::writeMessage);
    }

    private void writeMessage(Envelope envelope) {
        write(envelope);
    }

    private void write(Envelope m) {
        try {
            switch (format) {
                case PROTOBUF:
                    m.writeDelimitedTo(outputStream);
                    break;
                case NDJSON:
                    String json = jsonPrinter.print(m);
                    writer.write(json);
                    writer.write("\n");
                    writer.flush();
                    break;
                default:
                    throw new IllegalStateException("Unsupported format: " + format.name());
            }
            if (m.hasTestRunFinished()) {
                outputStream.close();
                writer.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    enum ProtobufFormat {
        NDJSON, PROTOBUF;
    }
}

