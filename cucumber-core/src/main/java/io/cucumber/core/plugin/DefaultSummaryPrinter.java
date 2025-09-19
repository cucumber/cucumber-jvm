package io.cucumber.core.plugin;

import io.cucumber.messages.types.Envelope;
import io.cucumber.plugin.ColorAware;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.prettyformatter.MessagesToSummaryWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import static io.cucumber.prettyformatter.Theme.cucumber;
import static io.cucumber.prettyformatter.Theme.plain;

public final class DefaultSummaryPrinter implements ColorAware, ConcurrentEventListener {

    private final OutputStream out;
    private MessagesToSummaryWriter writer;

    public DefaultSummaryPrinter() {
        this(new PrintStream(System.out) {
            @Override
            public void close() {
                // Don't close System.out
            }
        });
    }

    DefaultSummaryPrinter(OutputStream out) {
        this.out = out;
        this.writer = createBuilder().build(out);
    }

    private static MessagesToSummaryWriter.Builder createBuilder() {
        return MessagesToSummaryWriter.builder()
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
