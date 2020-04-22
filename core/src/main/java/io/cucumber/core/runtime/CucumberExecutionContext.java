package io.cucumber.core.runtime;

import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.exception.CompositeCucumberException;
import io.cucumber.core.exception.CucumberException;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.logging.Logger;
import io.cucumber.core.logging.LoggerFactory;
import io.cucumber.core.runner.Runner;
import io.cucumber.messages.Messages;
import io.cucumber.plugin.event.TestRunFinished;
import io.cucumber.plugin.event.TestRunStarted;
import io.cucumber.plugin.event.TestSourceRead;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static io.cucumber.core.runtime.Meta.makeMeta;
import static io.cucumber.messages.TimeConversion.javaInstantToTimestamp;
import static java.util.Collections.synchronizedList;

public final class CucumberExecutionContext {

    private static final Logger log = LoggerFactory.getLogger(CucumberExecutionContext.class);

    private final EventBus bus;
    private final ExitStatus exitStatus;
    private final RunnerSupplier runnerSupplier;
    private final List<Throwable> thrown = synchronizedList(new ArrayList<>());

    public CucumberExecutionContext(EventBus bus, ExitStatus exitStatus, RunnerSupplier runnerSupplier) {
        this.bus = bus;
        this.exitStatus = exitStatus;
        this.runnerSupplier = runnerSupplier;
    }

    public void emitMeta() {
        bus.send(Messages.Envelope.newBuilder()
            .setMeta(makeMeta())
            .build()
        );
    }

    public void startTestRun() {
        log.debug(() -> "Sending run test started event");
        Instant instant = bus.getInstant();
        bus.send(new TestRunStarted(instant));

        bus.send(Messages.Envelope.newBuilder()
            .setTestRunStarted(Messages.TestRunStarted.newBuilder()
                .setTimestamp(javaInstantToTimestamp(instant)))
            .build()
        );
    }

    public void finishTestRun() {
        log.debug(() -> "Sending test run finished event");
        CucumberException cucumberException = getException();
        emitTestRunFinished(cucumberException);
    }

    public CucumberException getException() {
        if (thrown.isEmpty()) {
            return null;
        }
        if (thrown.size() == 1) {
            return new CucumberException(thrown.get(0));
        }
        return new CompositeCucumberException(thrown);
    }

    private void emitTestRunFinished(CucumberException cucumberException) {
        Instant instant = bus.getInstant();
        bus.send(new TestRunFinished(instant, cucumberException));

        Messages.TestRunFinished.Builder testRunFinished = Messages.TestRunFinished.newBuilder()
            .setSuccess(exitStatus.isSuccess())
            .setTimestamp(javaInstantToTimestamp(instant));

        if (cucumberException != null) {
            testRunFinished.setMessage(cucumberException.getMessage());
        }
        bus.send(Messages.Envelope.newBuilder()
            .setTestRunFinished(testRunFinished)
            .build());
    }

    public void beforeFeature(Feature feature) {
        log.debug(() -> "Sending test source read event for " + feature.getUri());
        bus.send(new TestSourceRead(bus.getInstant(), feature.getUri(), feature.getSource()));
        bus.sendAll(feature.getParseEvents());
    }

    public void runTestCase(Consumer<Runner> execution) {
        Runner runner = getRunner();
        try {
            execution.accept(runner);
        } catch (Throwable e) {
            thrown.add(e);
            throw e;
        }
    }

    private Runner getRunner() {
        try {
            return runnerSupplier.get();
        } catch (Throwable e) {
            log.error(e, () -> "Unable to start Cucumber");
            thrown.add(e);
            throw e;
        }
    }
}
