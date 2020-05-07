package io.cucumber.junit;

import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.TestRunFinished;
import io.cucumber.plugin.event.TestRunStarted;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;

class InvokeMethodsAroundEventsTest {

    private static final List<String> events = new ArrayList<>();

    @AfterEach
    void afterClass() {
        events.clear();
    }

    @Test
    void invoke_methods_around_events() throws InitializationError {
        Cucumber cucumber = new Cucumber(BeforeAfterClass.class);
        cucumber.run(new RunNotifier());
        assertThat(events, contains("BeforeClass", "TestRunStarted", "TestRunFinished", "AfterClass"));
    }

    @CucumberOptions(plugin = "io.cucumber.junit.InvokeMethodsAroundEventsTest$TestRunStartedFinishedListener")
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
            publisher.registerHandlerFor(TestRunStarted.class, event -> events.add("TestRunStarted"));
            publisher.registerHandlerFor(TestRunFinished.class, event -> events.add("TestRunFinished"));
        }

    }

}
