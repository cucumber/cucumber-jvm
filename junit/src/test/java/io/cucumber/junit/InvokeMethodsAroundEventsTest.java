package io.cucumber.junit;

import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.TestCaseFinished;
import io.cucumber.plugin.event.TestCaseStarted;
import io.cucumber.plugin.event.TestRunFinished;
import io.cucumber.plugin.event.TestRunStarted;
import io.cucumber.plugin.event.TestSourceRead;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static io.cucumber.junit.StubBackendProviderService.callbacks;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;

class InvokeMethodsAroundEventsTest {

    private static final List<String> events = new ArrayList<>();

    private final Consumer<String> callback = events::add;

    @BeforeEach
    void before() {
        callbacks.add(callback);
    }

    @AfterEach
    void after() {
        events.clear();
        callbacks.remove(callback);
    }

    @Test
    void invoke_methods_around_events() throws InitializationError {
        Cucumber cucumber = new Cucumber(BeforeAfterClass.class);
        cucumber.run(new RunNotifier());
        assertThat(events, contains(
            "BeforeClass",
            "TestRunStarted",
            "BeforeAll",
            "TestSourceRead",
            "TestCaseStarted",
            "Before",
            "Step",
            "Step",
            "Step",
            "After",
            "TestCaseFinished",
            "TestCaseStarted",
            "Before",
            "Step",
            "Step",
            "Step",
            "After",
            "TestCaseFinished",
            "TestSourceRead",
            "TestCaseStarted",
            "Before",
            "Step",
            "Step",
            "Step",
            "After",
            "TestCaseFinished",
            "AfterAll",
            "TestRunFinished",
            "AfterClass"));
    }

    @CucumberOptions(
            plugin = "io.cucumber.junit.InvokeMethodsAroundEventsTest$TestRunStartedFinishedListener",
            features = { "classpath:io/cucumber/junit/rule.feature", "classpath:io/cucumber/junit/single.feature" })
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
            publisher.registerHandlerFor(TestSourceRead.class, event -> events.add("TestSourceRead"));
            publisher.registerHandlerFor(TestCaseStarted.class, event -> events.add("TestCaseStarted"));
            publisher.registerHandlerFor(TestCaseFinished.class, event -> events.add("TestCaseFinished"));
        }

    }

}
