package io.cucumber.core.plugin;

import io.cucumber.messages.internal.com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.messages.types.Envelope;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.EventPublisher;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

public final class MessageFormatter implements ConcurrentEventListener {

    private final Writer writer;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MessageFormatter(OutputStream outputStream) {
        this.writer = new UTF8OutputStreamWriter(outputStream);
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(Envelope.class, this::writeMessage);
    }

    private void writeMessage(Envelope envelope) {
        try {
            objectMapper.writeValue(writer, envelope);
            writer.write("\n");
            writer.flush();
            if (envelope.getTestRunFinished() != null) {
                writer.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
