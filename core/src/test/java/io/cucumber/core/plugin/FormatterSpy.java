package io.cucumber.core.plugin;

import io.cucumber.plugin.event.EventHandler;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.TestCaseFinished;
import io.cucumber.plugin.event.TestCaseStarted;
import io.cucumber.plugin.event.TestRunFinished;
import io.cucumber.plugin.event.TestStepFinished;
import io.cucumber.plugin.event.TestStepStarted;
import io.cucumber.plugin.EventListener;

public class FormatterSpy implements EventListener {
    private final StringBuilder calls = new StringBuilder();
    private final EventHandler<TestCaseStarted> testCaseStartedHandler = event -> calls.append("TestCase started\n");
    private final EventHandler<TestCaseFinished> testCaseFinishedHandler = event -> calls.append("TestCase finished\n");
    private final EventHandler<TestStepStarted> testStepStartedHandler = event -> calls.append("  TestStep started\n");
    private final EventHandler<TestStepFinished> testStepFinishedHandler = event -> calls.append("  TestStep finished\n");
    private EventHandler<TestRunFinished> runFinishHandler = event -> calls.append("TestRun finished\n");

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(TestCaseStarted.class, testCaseStartedHandler);
        publisher.registerHandlerFor(TestCaseFinished.class, testCaseFinishedHandler);
        publisher.registerHandlerFor(TestStepStarted.class, testStepStartedHandler);
        publisher.registerHandlerFor(TestStepFinished.class, testStepFinishedHandler);
        publisher.registerHandlerFor(TestRunFinished.class, runFinishHandler);
    }

    @Override
    public String toString() {
        return calls.toString();
    }
}
