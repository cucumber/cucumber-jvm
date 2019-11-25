package io.cucumber.core.runner;

import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.feature.CucumberPickle;
import io.cucumber.plugin.event.Result;
import io.cucumber.plugin.event.Status;
import io.cucumber.plugin.event.TestCaseFinished;
import io.cucumber.plugin.event.TestCaseStarted;
import io.cucumber.plugin.event.TestStep;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

final class TestCase implements io.cucumber.plugin.event.TestCase {
    private final CucumberPickle pickle;
    private final List<PickleStepTestStep> testSteps;
    private final boolean dryRun;
    private final List<HookTestStep> beforeHooks;
    private final List<HookTestStep> afterHooks;

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
        Instant start = bus.getInstant();
        bus.send(new TestCaseStarted(start, this));
        TestCaseState state = new TestCaseState(bus, this);

        for (HookTestStep before : beforeHooks) {
            skipNextStep |= before.run(this, bus, state, dryRun);
        }

        for (PickleStepTestStep step : testSteps) {
            skipNextStep |= step.run(this, bus, state, skipNextStep);
        }

        for (HookTestStep after : afterHooks) {
            after.run(this, bus, state, dryRun);
        }

        Instant stop = bus.getInstant();
        Duration duration = Duration.between(start, stop);
        Status status = Status.valueOf(state.getStatus().name());
        Result result = new Result(status, duration, state.getError());
        bus.send(new TestCaseFinished(stop, this, result));
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
    public Integer getLine() {
        return pickle.getLocation().getLine();
    }

    private String fileColonLine(Integer line) {
        return pickle.getUri().getSchemeSpecificPart() + ":" + line;
    }

    @Override
    public List<String> getTags() {
        return pickle.getTags();
    }
}
