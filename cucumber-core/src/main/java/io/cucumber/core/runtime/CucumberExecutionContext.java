package io.cucumber.core.runtime;

import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.logging.Logger;
import io.cucumber.core.logging.LoggerFactory;
import io.cucumber.core.runner.Runner;
import io.cucumber.messages.ProtocolVersion;
import io.cucumber.messages.types.Ci;
import io.cucumber.messages.types.Envelope;
import io.cucumber.messages.types.Git;
import io.cucumber.messages.types.Meta;
import io.cucumber.messages.types.Product;
import io.cucumber.plugin.event.Result;
import io.cucumber.plugin.event.Status;
import io.cucumber.plugin.event.TestRunFinished;
import io.cucumber.plugin.event.TestRunStarted;
import io.cucumber.plugin.event.TestSourceParsed;
import io.cucumber.plugin.event.TestSourceRead;

import java.time.Duration;
import java.time.Instant;
import java.util.ResourceBundle;
import java.util.function.Consumer;

import static io.cucumber.cienvironment.DetectCiEnvironment.detectCiEnvironment;
import static io.cucumber.core.exception.ExceptionUtils.printStackTrace;
import static io.cucumber.core.exception.ExceptionUtils.throwAsUncheckedException;
import static io.cucumber.core.exception.UnrecoverableExceptions.rethrowIfUnrecoverable;
import static io.cucumber.messages.Convertor.toMessage;
import static java.util.Collections.singletonList;

public final class CucumberExecutionContext {

    private static final String VERSION = ResourceBundle.getBundle("io.cucumber.core.version")
            .getString("cucumber-jvm.version");
    private static final Logger log = LoggerFactory.getLogger(CucumberExecutionContext.class);

    private final EventBus bus;
    private final ExitStatus exitStatus;
    private final RunnerSupplier runnerSupplier;
    private final RethrowingThrowableCollector collector = new RethrowingThrowableCollector();
    private Instant start;

    public CucumberExecutionContext(EventBus bus, ExitStatus exitStatus, RunnerSupplier runnerSupplier) {
        this.bus = bus;
        this.exitStatus = exitStatus;
        this.runnerSupplier = runnerSupplier;
    }

    @FunctionalInterface
    public interface ThrowingRunnable {
        void run() throws Throwable;
    }

    public void runInContext(ThrowingRunnable inContextExecution) {
        startTestRun();
        execute(() -> {
            runBeforeAllHooks();
            inContextExecution.run();
        });
        try {
            execute(this::runAfterAllHooks);
        } finally {
            finishTestRun();
        }
        Throwable throwable = getThrowable();
        if (throwable != null) {
            throwAsUncheckedException(throwable);
        }
    }

    private void execute(ThrowingRunnable runnable) {
        try {
            runnable.run();
        } catch (Throwable t) {
            // Collected in CucumberExecutionContext
            rethrowIfUnrecoverable(t);
        }
    }

    public void startTestRun() {
        emitMeta();
        emitTestRunStarted();
    }

    private void emitMeta() {
        bus.send(Envelope.of(createMeta()));
    }

    private Meta createMeta() {
        return new Meta(
            ProtocolVersion.getVersion(),
            new Product("cucumber-jvm", VERSION),
            new Product(System.getProperty("java.vm.name"), System.getProperty("java.vm.version")),
            new Product(System.getProperty("os.name"), null),
            new Product(System.getProperty("os.arch"), null),
            detectCiEnvironment(System.getenv()).map(ci -> new Ci(
                ci.getName(),
                ci.getUrl(),
                ci.getBuildNumber().orElse(null),
                ci.getGit().map(git -> new Git(
                    git.getRemote(),
                    git.getRevision(),
                    git.getBranch().orElse(null),
                    git.getTag().orElse(null)))
                        .orElse(null)))
                    .orElse(null));
    }

    private void emitTestRunStarted() {
        log.debug(() -> "Sending run test started event");
        start = bus.getInstant();
        bus.send(new TestRunStarted(start));
        bus.send(Envelope.of(new io.cucumber.messages.types.TestRunStarted(toMessage(start))));
    }

    public void runBeforeAllHooks() {
        Runner runner = getRunner();
        collector.executeAndThrow(runner::runBeforeAllHooks);
    }

    public void runAfterAllHooks() {
        Runner runner = getRunner();
        collector.executeAndThrow(runner::runAfterAllHooks);
    }

    public void finishTestRun() {
        log.debug(() -> "Sending test run finished event");
        Throwable cucumberException = getThrowable();
        emitTestRunFinished(cucumberException);
    }

    public Throwable getThrowable() {
        return collector.getThrowable();
    }

    private void emitTestRunFinished(Throwable exception) {
        Instant instant = bus.getInstant();
        Result result = new Result(
            exception != null ? Status.FAILED : exitStatus.getStatus(),
            Duration.between(start, instant),
            exception);
        bus.send(new TestRunFinished(instant, result));

        io.cucumber.messages.types.TestRunFinished testRunFinished = new io.cucumber.messages.types.TestRunFinished(
            exception != null ? printStackTrace(exception) : null,
            exception == null && exitStatus.isSuccess(),
            toMessage(instant),
            exception == null ? null : toMessage(exception));
        bus.send(Envelope.of(testRunFinished));
    }

    public void beforeFeature(Feature feature) {
        log.debug(() -> "Sending test source read event for " + feature.getUri());
        bus.send(new TestSourceRead(bus.getInstant(), feature.getUri(), feature.getSource()));
        bus.send(new TestSourceParsed(bus.getInstant(), feature.getUri(), singletonList(feature)));
        bus.sendAll(feature.getParseEvents());
    }

    public void runTestCase(Consumer<Runner> execution) {
        Runner runner = getRunner();
        collector.executeAndThrow(() -> execution.accept(runner));
    }

    private Runner getRunner() {
        return collector.executeAndThrow(runnerSupplier::get);
    }

}
