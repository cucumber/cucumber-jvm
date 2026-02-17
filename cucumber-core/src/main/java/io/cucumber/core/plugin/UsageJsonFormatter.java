package io.cucumber.core.plugin;

import io.cucumber.messages.types.Envelope;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.Plugin;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.usageformatter.MessagesToUsageWriter;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Formatter to measure performance of steps as json.
 */
public final class UsageJsonFormatter implements Plugin, ConcurrentEventListener {

    private final MessagesToUsageWriter writer;

    // Used by PluginFactory
    @SuppressWarnings("WeakerAccess")
    public UsageJsonFormatter(OutputStream out) {
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

}
