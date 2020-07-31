package io.cucumber.core.runtime;

import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.exception.CompositeCucumberException;
import io.cucumber.core.exception.CucumberException;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.logging.Logger;
import io.cucumber.core.logging.LoggerFactory;
import io.cucumber.core.runner.Runner;
import io.cucumber.messages.Messages;
import io.cucumber.messages.Messages.Envelope;
import io.cucumber.plugin.event.Result;
import io.cucumber.plugin.event.Status;
import io.cucumber.plugin.event.TestRunFinished;
import io.cucumber.plugin.event.TestRunStarted;
import io.cucumber.plugin.event.TestSourceParsed;
import io.cucumber.plugin.event.TestSourceRead;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;

import static io.cucumber.core.exception.ExceptionUtils.throwAsUncheckedException;
import static io.cucumber.createmeta.CreateMeta.createMeta;
import static io.cucumber.messages.TimeConversion.javaInstantToTimestamp;
import static java.util.Collections.singletonList;
import static java.util.Collections.synchronizedList;

public final class CucumberExecutionContext {

    private static final String VERSION = ResourceBundle.getBundle("io.cucumber.core.version")
            .getString("cucumber-jvm.version");
    private static final Logger log = LoggerFactory.getLogger(CucumberExecutionContext.class);

    private final EventBus bus;
    private final ExitStatus exitStatus;
    private final RunnerSupplier runnerSupplier;
    private final List<Throwable> thrown = synchronizedList(new ArrayList<>());
    private Instant start;

    public CucumberExecutionContext(EventBus bus, ExitStatus exitStatus, RunnerSupplier runnerSupplier) {
        this.bus = bus;
        this.exitStatus = exitStatus;
        this.runnerSupplier = runnerSupplier;
    }

    public void startTestRun() {
        emitMeta();
        emitTestRunStarted();
    }

    private void emitMeta() {
        bus.send(Envelope.newBuilder()
                .setMeta(createMeta("cucumber-jvm", VERSION, System.getenv()))
                .build());
    }

    private void emitTestRunStarted() {
        log.debug(() -> "Sending run test started event");
        start = bus.getInstant();
        bus.send(new TestRunStarted(start));
        bus.send(Envelope.newBuilder()
                .setTestRunStarted(Messages.TestRunStarted.newBuilder()
                        .setTimestamp(javaInstantToTimestamp(start)))
                .build());
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
        Result result = new Result(
            cucumberException != null ? Status.FAILED : exitStatus.getStatus(),
            Duration.between(start, instant),
            cucumberException);
        bus.send(new TestRunFinished(instant, result));

        Messages.TestRunFinished.Builder testRunFinished = Messages.TestRunFinished.newBuilder()
                .setSuccess(exitStatus.isSuccess())
                .setTimestamp(javaInstantToTimestamp(instant));

        if (cucumberException != null) {
            testRunFinished.setMessage(cucumberException.getMessage());
        }
        bus.send(Envelope.newBuilder()
                .setTestRunFinished(testRunFinished)
                .build());
    }

    public void beforeFeature(Feature feature) {
        log.debug(() -> "Sending test source read event for " + feature.getUri());
        bus.send(new TestSourceRead(bus.getInstant(), feature.getUri(), feature.getSource()));
        bus.send(new TestSourceParsed(bus.getInstant(), feature.getUri(), singletonList(feature)));
        bus.sendAll(feature.getParseEvents());
    }

    public void runTestCase(Consumer<Runner> execution) {
        Runner runner = getRunner();
        try {
            execution.accept(runner);
        } catch (TestCaseResultObserver.TestCaseFailed e) {
            throwAsUncheckedException(e.getCause());
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
