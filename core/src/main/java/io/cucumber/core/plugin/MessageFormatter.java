package io.cucumber.core.plugin;

import io.cucumber.messages.Messages.Envelope;
import io.cucumber.messages.internal.com.google.protobuf.util.JsonFormat;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.EventPublisher;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

public final class MessageFormatter implements ConcurrentEventListener {

    private final Writer writer;
    private final JsonFormat.Printer jsonPrinter = JsonFormat.printer()
            .omittingInsignificantWhitespace();

    public MessageFormatter(OutputStream outputStream) {
        this.writer = new UTF8OutputStreamWriter(outputStream);
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(Envelope.class, this::writeMessage);
    }

    private void writeMessage(Envelope envelope) {
        try {
            jsonPrinter.appendTo(envelope, writer);
            writer.write("\n");
            writer.flush();
            if (envelope.hasTestRunFinished()) {
                writer.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
