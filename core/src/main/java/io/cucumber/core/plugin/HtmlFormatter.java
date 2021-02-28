package io.cucumber.core.plugin;

import io.cucumber.htmlformatter.MessagesToHtmlWriter;
import io.cucumber.messages.Messages.Envelope;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.EventPublisher;

import java.io.IOException;
import java.io.OutputStream;

public final class HtmlFormatter implements ConcurrentEventListener {

    private final MessagesToHtmlWriter writer;

    @SuppressWarnings("WeakerAccess") // Used by PluginFactory
    public HtmlFormatter(OutputStream out) throws IOException {
        this.writer = new MessagesToHtmlWriter(new UTF8OutputStreamWriter(out));
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(Envelope.class, this::write);
    }

    private void write(Envelope event) {
        // Workaround to reduce the size of the report
        // See: https://github.com/cucumber/cucumber/issues/1232
        if (event.hasStepDefinition() || event.hasHook() || event.hasParameterType()) {
            return;
        }

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
