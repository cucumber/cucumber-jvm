package io.cucumber.core.plugin;

import io.cucumber.messages.types.Envelope;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.Plugin;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.usageformatter.MessagesToUsageWriter;
import io.cucumber.usageformatter.UsageReportSerializer;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import static io.cucumber.usageformatter.UsageReportSerializer.PlainTextFeature.INCLUDE_STEPS;

/**
 * Formatter to measure performance of steps. Includes average and median step
 * duration.
 */
public final class UsageFormatter implements Plugin, ConcurrentEventListener {

    private final MessagesToUsageWriter writer;

    @SuppressWarnings("WeakerAccess") // Used by PluginFactory
    public UsageFormatter(OutputStream out) {
        String cwdUri = new File("").toPath().toUri().toString();
        this.writer = MessagesToUsageWriter.builder(UsageReportSerializer.builder()
                .feature(INCLUDE_STEPS, true)
                .maxStepsPerStepDefinition(5)
                .build())
                .removeUriPrefix(cwdUri)
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
