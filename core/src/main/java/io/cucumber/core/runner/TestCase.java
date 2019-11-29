package io.cucumber.core.runner;

import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.gherkin.CucumberPickle;
import io.cucumber.messages.Messages;
import io.cucumber.plugin.event.Result;
import io.cucumber.plugin.event.Status;
import io.cucumber.plugin.event.TestCaseFinished;
import io.cucumber.plugin.event.TestCaseStarted;
import io.cucumber.plugin.event.TestStep;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static io.cucumber.core.messages.MessageHelpers.toDuration;
import static io.cucumber.core.messages.MessageHelpers.toStatus;
import static io.cucumber.core.messages.MessageHelpers.toTimestamp;

final class TestCase implements io.cucumber.plugin.event.TestCase {
    private final CucumberPickle pickle;
    private final List<PickleStepTestStep> testSteps;
    private final boolean dryRun;
    private final List<HookTestStep> beforeHooks;
    private final List<HookTestStep> afterHooks;
    //TODO: Primitive obsession. Lets use UUIDs here.
    private final String id = UUID.randomUUID().toString();

    TestCase(List<PickleStepTestStep> testSteps,
             List<HookTestStep> beforeHooks,
             List<HookTestStep> afterHooks,
             CucumberPickle pickle,
             boolean dryRun) {
        this.testSteps = testSteps;
        this.beforeHooks = beforeHooks;
        this.afterHooks = afterHooks;
        this.pickle = pickle;
        this.dryRun = dryRun;
    }

    void run(EventBus bus) {
        boolean skipNextStep = this.dryRun;
        sendTestCaseMessage(bus);

        Instant start = bus.getInstant();
        bus.send(new TestCaseStarted(start, this));
        String testCaseStartedId = UUID.randomUUID().toString();
        sendTestCaseStartedMessage(bus, start, testCaseStartedId);

        TestCaseState state = new TestCaseState(bus, this);

        for (HookTestStep before : beforeHooks) {
            skipNextStep |= before.run(this, bus, state, dryRun, testCaseStartedId);
        }

        for (PickleStepTestStep step : testSteps) {
            skipNextStep |= step.run(this, bus, state, skipNextStep, testCaseStartedId);
        }

        for (HookTestStep after : afterHooks) {
            after.run(this, bus, state, dryRun, testCaseStartedId);
        }

        Instant stop = bus.getInstant();
        Duration duration = Duration.between(start, stop);
        Status status = Status.valueOf(state.getStatus().name());
        Result result = new Result(status, duration, state.getError());
        bus.send(new TestCaseFinished(stop, this, result));
        sendTestCaseFinishedMessage(bus, testCaseStartedId, stop, duration, status, result);
    }

    @Override
    public List<TestStep> getTestSteps() {
        List<TestStep> testSteps = new ArrayList<>(beforeHooks);
        for (PickleStepTestStep step : this.testSteps) {
            testSteps.addAll(step.getBeforeStepHookSteps());
            testSteps.add(step);
            testSteps.addAll(step.getAfterStepHookSteps());
        }
        testSteps.addAll(afterHooks);
        return testSteps;
    }

    @Override
    public String getName() {
        return pickle.getName();
    }

    @Override
    public String getScenarioDesignation() {
        return fileColonLine(getLine()) + " # " + getName();
    }

    @Override
    public URI getUri() {
        return pickle.getUri();
    }

    @Override
    public String getPickleId() {
        return pickle.getId();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Integer getLine() {
        return pickle.getLocation().getLine();
    }

    @Override
    public String getKeyword() {
        return pickle.getKeyword();
    }

    private String fileColonLine(Integer line) {
        return pickle.getUri().getSchemeSpecificPart() + ":" + line;
    }

    @Override
    public List<String> getTags() {
        return pickle.getTags();
    }

    private void sendTestCaseMessage(EventBus bus) {
        bus.send(Messages.Envelope.newBuilder()
            .setTestCase(Messages.TestCase.newBuilder()
                .setId(getId())
                .setPickleId(getPickleId())
                .addAllTestSteps(getTestSteps()
                    .stream()
                    .map(testStep -> {
                            Messages.TestCase.TestStep.Builder testStepBuilder = Messages.TestCase.TestStep
                                .newBuilder()
                                .setId(testStep.getId());

                            if (testStep instanceof HookTestStep) {
                                testStepBuilder.setHookId(testStep.getId());
                            } else if (testStep instanceof PickleStepTestStep) {
                                testStepBuilder
                                    .setPickleStepId(testStep.getPickleStepId())
                                    .addAllStepMatchArguments(testStep.getStepMatchArguments());
                            }
                            return testStepBuilder.build();
                        }
                    )
                    .collect(Collectors.toList())
                )
            ).build()
        );
    }

    private void sendTestCaseStartedMessage(EventBus bus, Instant start, String testCaseStartedId) {
        bus.send(Messages.Envelope.newBuilder()
            .setTestCaseStarted(Messages.TestCaseStarted.newBuilder()
                .setId(testCaseStartedId)
                .setTestCaseId(getId())
                .setTimestamp(toTimestamp(start))).build());
    }

    private void sendTestCaseFinishedMessage(EventBus bus, String testCaseStartedId, Instant stop, Duration duration, Status status, Result result) {
        Messages.TestResult.Builder testResultBuilder = Messages.TestResult.newBuilder()
            .setStatus(toStatus(status))
            .setDuration(toDuration(duration));

        if (result.getError() != null) {
            testResultBuilder.setMessage(toString(result.getError()));
        }

        bus.send(Messages.Envelope.newBuilder()
            .setTestCaseFinished(Messages.TestCaseFinished.newBuilder()
                .setTestCaseStartedId(testCaseStartedId)
                .setTimestamp(toTimestamp(stop))
                .setTestResult(testResultBuilder
                )
            ).build());
    }

    private static String toString(Throwable error) {
        StringWriter stringWriter = new StringWriter();
        error.printStackTrace(new PrintWriter(stringWriter));
        return stringWriter.toString();
    }
}
