package io.cucumber.core.plugin;

import io.cucumber.core.event.PickleStepTestStep;
import io.cucumber.core.event.EventHandler;
import io.cucumber.core.event.EventPublisher;
import io.cucumber.core.event.Status;
import io.cucumber.core.event.TestRunFinished;
import io.cucumber.core.event.TestStepFinished;
import io.cucumber.core.event.WriteEvent;

import java.util.HashMap;
import java.util.Map;

public final class ProgressFormatter implements ConcurrentEventListener, ColorAware {
    private static final Map<Status, Character> CHARS = new HashMap<Status, Character>() {{
        put(Status.PASSED, '.');
        put(Status.UNDEFINED, 'U');
        put(Status.PENDING, 'P');
        put(Status.SKIPPED, '-');
        put(Status.FAILED, 'F');
        put(Status.AMBIGUOUS, 'A');
    }};
    private static final Map<Status, AnsiEscapes> ANSI_ESCAPES = new HashMap<Status, AnsiEscapes>() {{
        put(Status.PASSED, AnsiEscapes.GREEN);
        put(Status.UNDEFINED, AnsiEscapes.YELLOW);
        put(Status.PENDING, AnsiEscapes.YELLOW);
        put(Status.SKIPPED, AnsiEscapes.CYAN);
        put(Status.FAILED, AnsiEscapes.RED);
        put(Status.AMBIGUOUS, AnsiEscapes.RED);
    }};

    private final NiceAppendable out;
    private boolean monochrome = false;
    private EventHandler<TestStepFinished> stepFinishedhandler = new EventHandler<TestStepFinished>() {
        @Override
        public void receive(TestStepFinished event) {
            handleTestStepFinished(event);
        }
    };
    private EventHandler<WriteEvent> writeHandler = new EventHandler<WriteEvent>() {
        @Override
        public void receive(WriteEvent event) {
            handleWrite(event);
        }
    };
    private EventHandler<TestRunFinished> runFinishHandler = new EventHandler<TestRunFinished>() {
        @Override
        public void receive(TestRunFinished event) {
            handleTestRunFinished();
        }
    };

    public ProgressFormatter(Appendable appendable) {
        out = new NiceAppendable(appendable);
    }

    @Override
    public void setMonochrome(boolean monochrome) {
        this.monochrome = monochrome;
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(TestStepFinished.class, stepFinishedhandler);
        publisher.registerHandlerFor(WriteEvent.class, writeHandler);
        publisher.registerHandlerFor(TestRunFinished.class, runFinishHandler);
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
