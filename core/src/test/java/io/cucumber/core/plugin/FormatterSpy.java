package io.cucumber.core.plugin;

import io.cucumber.core.api.event.EventHandler;
import io.cucumber.core.api.event.EventListener;
import io.cucumber.core.api.event.EventPublisher;
import io.cucumber.core.api.event.TestCaseFinished;
import io.cucumber.core.api.event.TestCaseStarted;
import io.cucumber.core.api.event.TestRunFinished;
import io.cucumber.core.api.event.TestStepFinished;
import io.cucumber.core.api.event.TestStepStarted;

public class FormatterSpy implements EventListener {
    StringBuilder calls = new StringBuilder();
    private final EventHandler<TestCaseStarted> testCaseStartedHandler = new EventHandler<TestCaseStarted>() {
        @Override
        public void receive(TestCaseStarted event) {
            calls.append("TestCase started\n");
        }
    };
    private final EventHandler<TestCaseFinished> testCaseFinishedHandler = new EventHandler<TestCaseFinished>() {
        @Override
        public void receive(TestCaseFinished event) {
            calls.append("TestCase finished\n");
        }
    };
    private final EventHandler<TestStepStarted> testStepStartedHandler = new EventHandler<TestStepStarted>() {
        @Override
        public void receive(TestStepStarted event) {
            calls.append("  TestStep started\n");
        }
    };
    private final EventHandler<TestStepFinished> testStepFinishedHandler = new EventHandler<TestStepFinished>() {
        @Override
        public void receive(TestStepFinished event) {
            calls.append("  TestStep finished\n");
        }
    };
    private EventHandler<TestRunFinished> runFinishHandler = new EventHandler<TestRunFinished>() {

        @Override
        public void receive(TestRunFinished event) {
            calls.append("TestRun finished\n");
        }
    };

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
