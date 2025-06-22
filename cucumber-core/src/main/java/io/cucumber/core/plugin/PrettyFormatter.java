package io.cucumber.core.plugin;

import io.cucumber.messages.types.Envelope;
import io.cucumber.plugin.ColorAware;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.prettyformatter.MessagesToPrettyWriter;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Prints a pretty report of the scenario execution as it happens.
 * <p>
 * When scenarios are executed concurrently the output will interleave. This is
 * to be expected.
 */
public final class PrettyFormatter implements ConcurrentEventListener, ColorAware {

    private MessagesToPrettyWriter writer;

    public PrettyFormatter(OutputStream out) {
        String cwdUri = new File("").toURI().toString();
        this.writer = new MessagesToPrettyWriter(out)
                .withRemovePathPrefix(cwdUri);
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
            writer.close();
        }
    }

    @Override
    public void setMonochrome(boolean monochrome) {
        if (monochrome) {
            writer = writer.withNoAnsiColors();
        }
    }

}
