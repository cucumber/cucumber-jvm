package io.cucumber.core.plugin;

import io.cucumber.messages.types.Envelope;
import io.cucumber.plugin.ColorAware;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.usageformatter.MessagesToUsageWriter;
import io.cucumber.usageformatter.UnusedReportSerializer;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Formatter to measure performance of steps. Includes average and median step
 * duration.
 */
public final class UnusedStepsSummaryPrinter implements ColorAware, ConcurrentEventListener {

    private final MessagesToUsageWriter writer;

    // Used by PluginFactory
    @SuppressWarnings("WeakerAccess") 
    public UnusedStepsSummaryPrinter(OutputStream out) {
        this.writer = MessagesToUsageWriter.builder(new UnusedReportSerializer()).build(out);
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

    @Override
    public void setMonochrome(boolean monochrome) {
        // no-op, no colors printed
    }
}
