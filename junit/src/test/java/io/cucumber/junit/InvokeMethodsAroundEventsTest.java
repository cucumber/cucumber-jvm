package io.cucumber.junit;

import cucumber.api.event.ConcurrentEventListener;
import cucumber.api.event.EventHandler;
import cucumber.api.event.EventPublisher;
import cucumber.api.event.TestRunFinished;
import cucumber.api.event.TestRunStarted;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.junit.Assert.assertThat;

public class InvokeMethodsAroundEventsTest {

    private static final List<String> events = new ArrayList<>();

    private static EventHandler<TestRunStarted> testRunStartedEventHandler = new EventHandler<TestRunStarted>() {
        @Override
        public void receive(TestRunStarted event) {
            events.add("TestRunStarted");
        }
    };
    private static EventHandler<TestRunFinished> testRunFinishedEventHandler = new EventHandler<TestRunFinished>() {
        @Override
        public void receive(TestRunFinished event) {
            events.add("TestRunFinished");
        }
    };

    @AfterClass
    public static void afterClass() {
        events.clear();
    }

    @Test
    public void invoke_methods_around_events() throws InitializationError {
        Cucumber cucumber = new Cucumber(BeforeAfterClass.class);
        cucumber.run(new RunNotifier());
        assertThat(events, contains("BeforeClass", "TestRunStarted", "TestRunFinished", "AfterClass"));
    }

    @CucumberOptions(plugin = {"io.cucumber.junit.InvokeMethodsAroundEventsTest$TestRunStartedFinishedListener"})
    public static class BeforeAfterClass {

        @BeforeClass
        public static void beforeClass() {
            events.add("BeforeClass");

        }

        @AfterClass
        public static void afterClass() {
            events.add("AfterClass");
        }
    }

    @SuppressWarnings("unused") // Used as a plugin by BeforeAfterClass
    public static class TestRunStartedFinishedListener implements ConcurrentEventListener {

        @Override
        public void setEventPublisher(EventPublisher publisher) {
            publisher.registerHandlerFor(TestRunStarted.class, testRunStartedEventHandler);
            publisher.registerHandlerFor(TestRunFinished.class, testRunFinishedEventHandler);
        }

    }
}
