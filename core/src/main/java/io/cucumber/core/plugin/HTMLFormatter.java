package io.cucumber.core.plugin;

import io.cucumber.htmlformatter.MessagesToHtmlWriter;
import io.cucumber.messages.Messages.Envelope;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.EventPublisher;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public final class HTMLFormatter implements ConcurrentEventListener {

    private final MessagesToHtmlWriter writer;

    @SuppressWarnings("WeakerAccess") // Used by PluginFactory
    public HTMLFormatter(OutputStream out) throws IOException {
        this.writer = new MessagesToHtmlWriter(new OutputStreamWriter(out));
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
        if (event.hasTestRunFinished()) {
            try {
                writer.close();
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
    }
}
