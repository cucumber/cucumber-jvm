package io.cucumber.core.plugin;

import io.cucumber.plugin.ColorAware;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.PickleStepTestStep;
import io.cucumber.plugin.event.Status;
import io.cucumber.plugin.event.TestRunFinished;
import io.cucumber.plugin.event.TestStepFinished;
import io.cucumber.plugin.event.WriteEvent;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public final class ProgressFormatter implements ConcurrentEventListener, ColorAware {

    private static final Map<Status, Character> CHARS = new HashMap<Status, Character>() {
        {
            put(Status.PASSED, '.');
            put(Status.UNDEFINED, 'U');
            put(Status.PENDING, 'P');
            put(Status.SKIPPED, '-');
            put(Status.FAILED, 'F');
            put(Status.AMBIGUOUS, 'A');
        }
    };
    private static final Map<Status, AnsiEscapes> ANSI_ESCAPES = new HashMap<Status, AnsiEscapes>() {
        {
            put(Status.PASSED, AnsiEscapes.GREEN);
            put(Status.UNDEFINED, AnsiEscapes.YELLOW);
            put(Status.PENDING, AnsiEscapes.YELLOW);
            put(Status.SKIPPED, AnsiEscapes.CYAN);
            put(Status.FAILED, AnsiEscapes.RED);
            put(Status.AMBIGUOUS, AnsiEscapes.RED);
        }
    };

    private final NiceAppendable out;
    private boolean monochrome = false;

    public ProgressFormatter(OutputStream out) {
        this.out = new NiceAppendable(new UTF8OutputStreamWriter(out));
    }

    @Override
    public void setMonochrome(boolean monochrome) {
        this.monochrome = monochrome;
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(TestStepFinished.class, this::handleTestStepFinished);
        publisher.registerHandlerFor(WriteEvent.class, this::handleWrite);
        publisher.registerHandlerFor(TestRunFinished.class, event -> handleTestRunFinished());
    }

    private void handleTestStepFinished(TestStepFinished event) {
        if (event.getTestStep() instanceof PickleStepTestStep || event.getResult().getStatus().is(Status.FAILED)) {
            if (!monochrome) {
                ANSI_ESCAPES.get(event.getResult().getStatus()).appendTo(out);
            }
            out.append(CHARS.get(event.getResult().getStatus()));
            if (!monochrome) {
                AnsiEscapes.RESET.appendTo(out);
            }
        }
    }

    private void handleWrite(WriteEvent event) {
        out.append(event.getText());
    }

    private void handleTestRunFinished() {
        out.println();
        out.close();
    }

}
