package io.cucumber.core.plugin;

import io.cucumber.messages.types.Envelope;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.EventPublisher;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

import static io.cucumber.core.plugin.MessagesToJsonWriter.builder;

public final class JsonFormatter implements ConcurrentEventListener {

    private final MessagesToJsonWriter writer;

    public JsonFormatter(OutputStream out) {
        URI cwdUri = new File("").toURI();
        this.writer = builder(Jackson.OBJECT_MAPPER::writeValue)
                .relativizeAgainst(cwdUri)
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
}
