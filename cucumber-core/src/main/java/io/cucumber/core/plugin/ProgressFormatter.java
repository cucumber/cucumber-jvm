package io.cucumber.core.plugin;

import io.cucumber.messages.types.Envelope;
import io.cucumber.plugin.ColorAware;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.prettyformatter.MessagesToProgressWriter;

import java.io.IOException;
import java.io.OutputStream;

import static io.cucumber.prettyformatter.Theme.cucumber;
import static io.cucumber.prettyformatter.Theme.plain;

/**
 * Renders a rudimentary progress bar.
 * <p>
 * Each character in the bar represents either a step or hook. The status of
 * that step or hook is indicated by the character and its color.
 */
public final class ProgressFormatter implements ConcurrentEventListener, ColorAware {

    private final OutputStream out;
    private MessagesToProgressWriter writer;

    public ProgressFormatter(OutputStream out) {
        this.out = out;
        this.writer = createBuilder().build(out);
    }

    private static MessagesToProgressWriter.Builder createBuilder() {
        return MessagesToProgressWriter.builder()
                .theme(cucumber());
    }

    @Override
    public void setMonochrome(boolean monochrome) {
        if (monochrome) {
            writer = createBuilder().theme(plain()).build(out);
        }
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
}
