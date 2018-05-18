package cucumber.runtime;

import cucumber.api.Result;
import cucumber.api.event.EventHandler;
import cucumber.api.event.EventListener;
import cucumber.api.event.EventPublisher;
import cucumber.api.event.TestCaseFinished;

import java.util.ArrayList;
import java.util.List;

import static cucumber.api.Result.SEVERITY;
import static java.util.Collections.max;

class ExitStatus implements EventListener {
    private static final byte ERRORS = 0x1;

    private final List<Result> results = new ArrayList<Result>();

    private final EventHandler<TestCaseFinished> testCaseFinishedHandler = new EventHandler<TestCaseFinished>() {
        @Override
        public void receive(TestCaseFinished event) {
            results.add(event.result);
        }
    };

    ExitStatus() {
    }


    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(TestCaseFinished.class, testCaseFinishedHandler);
    }

    public byte exitStatus(boolean isStrict) {
        return results.isEmpty() || max(results, SEVERITY).isOk(isStrict) ? 0x0 : ERRORS;
    }
}
