package io.cucumber.core.plugin;

import io.cucumber.messages.types.Envelope;
import io.cucumber.plugin.ColorAware;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.prettyformatter.Formatter;
import io.cucumber.prettyformatter.MessagesToPrettyWriter;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Prints a pretty report of the scenario execution as it happens.
 * <p>
 * When scenarios are executed concurrently the output will interleave. This is
 * to be expected.
 */
public final class PrettyFormatter implements ConcurrentEventListener, ColorAware {

    private final OutputStream out;
    private MessagesToPrettyWriter writer;

    public PrettyFormatter(OutputStream out) {
        this.out = out;
        this.writer = new MessagesToPrettyWriter(out, Formatter.ansi());
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
        writer = new MessagesToPrettyWriter(out, 
                monochrome ? Formatter.noAnsi() : Formatter.ansi()
        );
    }

}
