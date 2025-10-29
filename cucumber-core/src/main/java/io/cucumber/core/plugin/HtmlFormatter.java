package io.cucumber.core.plugin;

import io.cucumber.htmlformatter.MessagesToHtmlWriter;
import io.cucumber.messages.types.Envelope;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.EventPublisher;

import java.io.IOException;
import java.io.OutputStream;

public final class HtmlFormatter implements ConcurrentEventListener {

    private final MessagesToHtmlWriter writer;

    @SuppressWarnings("WeakerAccess") // Used by PluginFactory
    public HtmlFormatter(OutputStream out) throws IOException {
        this.writer = new MessagesToHtmlWriter(out, Jackson.OBJECT_MAPPER::writeValue);
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
