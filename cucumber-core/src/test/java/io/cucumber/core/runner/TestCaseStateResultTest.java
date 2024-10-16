package io.cucumber.core.runner;

import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.feature.TestFeatureParser;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.plugin.event.EmbedEvent;
import io.cucumber.plugin.event.EventHandler;
import io.cucumber.plugin.event.Result;
import io.cucumber.plugin.event.Status;
import io.cucumber.plugin.event.WriteEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static io.cucumber.core.backend.Status.FAILED;
import static io.cucumber.core.backend.Status.PASSED;
import static io.cucumber.core.backend.Status.SKIPPED;
import static io.cucumber.core.backend.Status.UNDEFINED;
import static java.time.Duration.ZERO;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestCaseStateResultTest {

    private final Feature feature = TestFeatureParser.parse("file:path/file.feature", "" +
            "Feature: Test feature\n" +
            "  Scenario: Test scenario\n" +
            "     Given I have 4 cukes in my belly\n");
    private final MockEventBus bus = new MockEventBus();
    private final TestCaseState s = new TestCaseState(
        bus,
        UUID.randomUUID(),
        new TestCase(
            UUID.randomUUID(),
            Collections.emptyList(),
            Collections.emptyList(),
            Collections.emptyList(),
            feature.getPickles().get(0),
            false));

    @BeforeEach
    void setup() {
        s.setCurrentTestStepId(UUID.randomUUID());
    }

    @Test
    void no_steps_is_passed() {
        assertThat(s.getStatus(), is(equalTo(PASSED)));
    }

    @Test
    void one_passed_step_is_passed() {
        s.add(new Result(Status.PASSED, ZERO, null));
        assertThat(s.getStatus(), is(equalTo(PASSED)));
    }

    @Test
    void passed_failed_pending_undefined_skipped_is_failed() {
        s.add(new Result(Status.PASSED, ZERO, null));
        s.add(new Result(Status.FAILED, ZERO, null));
        s.add(new Result(Status.PENDING, ZERO, null));
        s.add(new Result(Status.UNDEFINED, ZERO, null));
        s.add(new Result(Status.SKIPPED, ZERO, null));

        assertAll(
            () -> assertThat(s.getStatus(), is(equalTo(FAILED))),
            () -> assertTrue(s.isFailed()));
    }

    @Test
    void passed_and_skipped_is_skipped_although_we_cant_have_skipped_without_undefined_or_pending() {
        s.add(new Result(Status.PASSED, ZERO, null));
        s.add(new Result(Status.SKIPPED, ZERO, null));

        assertAll(
            () -> assertThat(s.getStatus(), is(equalTo(SKIPPED))),
            () -> assertFalse(s.isFailed()));
    }

    @Test
    void passed_pending_undefined_skipped_is_pending() {
        s.add(new Result(Status.PASSED, ZERO, null));
        s.add(new Result(Status.UNDEFINED, ZERO, null));
        s.add(new Result(Status.PENDING, ZERO, null));
        s.add(new Result(Status.SKIPPED, ZERO, null));

        assertAll(
            () -> assertThat(s.getStatus(), is(equalTo(UNDEFINED))),
            () -> assertFalse(s.isFailed()));
    }

    @Test
    void passed_undefined_skipped_is_undefined() {
        s.add(new Result(Status.PASSED, ZERO, null));
        s.add(new Result(Status.UNDEFINED, ZERO, null));
        s.add(new Result(Status.SKIPPED, ZERO, null));

        assertAll(
            () -> assertThat(s.getStatus(), is(equalTo(UNDEFINED))),
            () -> assertFalse(s.isFailed()));
    }

    @SuppressWarnings("deprecation")
    @Test
    void embeds_data() {
        bus.events.clear();
        byte[] data = new byte[] { 1, 2, 3 };

        s.attach(data, "bytes/foo", null);

        assertInstanceOf(EmbedEvent.class, bus.events.get(0));
        assertEquals("bytes/foo", ((EmbedEvent) bus.events.get(0)).getMediaType());
        assertEquals(data, ((EmbedEvent) bus.events.get(0)).getData());
    }

    @Test
    void prints_output() {
        bus.events.clear();
        s.log("Hi");
        assertInstanceOf(WriteEvent.class, bus.events.get(0));
        assertEquals("Hi", ((WriteEvent) bus.events.get(0)).getText());
    }

    @Test
    void failed_followed_by_pending_yields_failed_error() {
        Throwable failedError = new Throwable();
        Throwable pendingError = new Throwable();

        s.add(new Result(Status.FAILED, ZERO, failedError));
        s.add(new Result(Status.PENDING, ZERO, pendingError));

        assertThat(s.getError(), sameInstance(failedError));
    }

    @Test
    void pending_followed_by_failed_yields_failed_error() {
        Throwable pendingError = new Throwable();
        Throwable failedError = new Throwable();

        s.add(new Result(Status.PENDING, ZERO, pendingError));
        s.add(new Result(Status.FAILED, ZERO, failedError));

        assertThat(s.getError(), sameInstance(failedError));
    }

    private static class MockEventBus implements EventBus {
        List<Object> events = new ArrayList<>();

        @Override
        public Instant getInstant() {
            return Instant.now();
        }

        @Override
        public UUID generateId() {
            return null;
        }

        @Override
        public <T> void send(T event) {
            this.events.add(event);
        }

        @Override
        public <T> void sendAll(Iterable<T> queue) {
        }

        @Override
        public <T> void registerHandlerFor(Class<T> eventType, EventHandler<T> handler) {

        }

        @Override
        public <T> void removeHandlerFor(Class<T> eventType, EventHandler<T> handler) {

        }
    }
}
