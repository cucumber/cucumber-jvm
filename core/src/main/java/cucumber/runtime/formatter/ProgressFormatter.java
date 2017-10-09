package cucumber.runtime.formatter;

import cucumber.api.Result;
import cucumber.api.event.EventHandler;
import cucumber.api.event.EventPublisher;
import cucumber.api.event.TestRunFinished;
import cucumber.api.event.TestStepFinished;
import cucumber.api.event.WriteEvent;
import cucumber.api.formatter.AnsiEscapes;
import cucumber.api.formatter.ColorAware;
import cucumber.api.formatter.Formatter;
import cucumber.api.formatter.NiceAppendable;

import java.util.HashMap;
import java.util.Map;

class ProgressFormatter implements Formatter, ColorAware {
    private static final Map<Result.Type, Character> CHARS = new HashMap<Result.Type, Character>() {{
        put(Result.Type.PASSED, '.');
        put(Result.Type.UNDEFINED, 'U');
        put(Result.Type.PENDING, 'P');
        put(Result.Type.SKIPPED, '-');
        put(Result.Type.FAILED, 'F');
        put(Result.Type.AMBIGUOUS, 'A');
    }};
    private static final Map<Result.Type, AnsiEscapes> ANSI_ESCAPES = new HashMap<Result.Type, AnsiEscapes>() {{
        put(Result.Type.PASSED, AnsiEscapes.GREEN);
        put(Result.Type.UNDEFINED, AnsiEscapes.YELLOW);
        put(Result.Type.PENDING, AnsiEscapes.YELLOW);
        put(Result.Type.SKIPPED, AnsiEscapes.CYAN);
        put(Result.Type.FAILED, AnsiEscapes.RED);
        put(Result.Type.AMBIGUOUS, AnsiEscapes.RED);
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
        if (!event.testStep.isHook() || event.result.is(Result.Type.FAILED)) {
            if (!monochrome) {
                ANSI_ESCAPES.get(event.result.getStatus()).appendTo(out);
            }
            out.append(CHARS.get(event.result.getStatus()));
            if (!monochrome) {
                AnsiEscapes.RESET.appendTo(out);
            }
        }
    }

    private void handleWrite(WriteEvent event) {
        out.append(event.text);
    }

    private void handleTestRunFinished() {
        out.println();
        out.close();
    }
}
