package cucumber.runtime;

import cucumber.api.Result;
import cucumber.api.event.EventHandler;
import cucumber.api.event.EventListener;
import cucumber.api.event.EventPublisher;
import cucumber.api.event.TestCaseFinished;
import io.cucumber.core.options.RuntimeOptions;

import java.util.ArrayList;
import java.util.List;

import static cucumber.api.Result.SEVERITY;
import static java.util.Collections.max;
import static java.util.Collections.min;

public class ExitStatus implements EventListener {
    private static final byte DEFAULT = 0x0;
    private static final byte ERRORS = 0x1;

    private final List<Result> results = new ArrayList<Result>();
    private final RuntimeOptions runtimeOptions;

    private final EventHandler<TestCaseFinished> testCaseFinishedHandler = new EventHandler<TestCaseFinished>() {
        @Override
        public void receive(TestCaseFinished event) {
            results.add(event.result);
        }
    };

    public ExitStatus(RuntimeOptions runtimeOptions) {
        this.runtimeOptions = runtimeOptions;
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(TestCaseFinished.class, testCaseFinishedHandler);
    }

    public byte exitStatus() {
        if (results.isEmpty()) { return DEFAULT; }

        if (runtimeOptions.isWip()) {
            return min(results, SEVERITY).is(Result.Type.PASSED) ? ERRORS : DEFAULT;
        }

        return max(results, SEVERITY).isOk(runtimeOptions.isStrict()) ? DEFAULT : ERRORS;
    }
}
