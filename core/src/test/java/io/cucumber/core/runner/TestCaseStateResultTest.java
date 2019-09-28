package io.cucumber.core.runner;

import io.cucumber.plugin.event.EmbedEvent;
import io.cucumber.plugin.event.Result;
import io.cucumber.plugin.event.Status;
import io.cucumber.plugin.event.WriteEvent;
import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.feature.CucumberFeature;
import io.cucumber.core.feature.TestFeatureParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;

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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TestCaseStateResultTest {

    private final CucumberFeature feature = TestFeatureParser.parse("file:path/file.feature", "" +
        "Feature: Test feature\n" +
        "  Scenario: Test scenario\n" +
        "     Given I have 4 cukes in my belly\n"
    );
    private final EventBus bus = mock(EventBus.class);
    private final TestCaseState s = new TestCaseState(
        bus,
        new TestCase(
            Collections.emptyList(),
            Collections.emptyList(),
            Collections.emptyList(),
            feature.getPickles().get(0),
            false
        )
    );

    @BeforeEach
    void setup() {
        when(bus.getInstant()).thenReturn(Instant.now());
    }

    @Test
    void no_steps_is_undefined() {
        assertThat(s.getStatus(), is(equalTo(UNDEFINED)));
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

        assertAll("Checking Scenario",
            () -> assertThat(s.getStatus(), is(equalTo(FAILED))),
            () -> assertTrue(s.isFailed())
        );
    }

    @Test
    void passed_and_skipped_is_skipped_although_we_cant_have_skipped_without_undefined_or_pending() {
        s.add(new Result(Status.PASSED, ZERO, null));
        s.add(new Result(Status.SKIPPED, ZERO, null));

        assertAll("Checking Scenario",
            () -> assertThat(s.getStatus(), is(equalTo(SKIPPED))),
            () -> assertFalse(s.isFailed())
        );
    }

    @Test
    void passed_pending_undefined_skipped_is_pending() {
        s.add(new Result(Status.PASSED, ZERO, null));
        s.add(new Result(Status.UNDEFINED, ZERO, null));
        s.add(new Result(Status.PENDING, ZERO, null));
        s.add(new Result(Status.SKIPPED, ZERO, null));

        assertAll("Checking Scenario",
            () -> assertThat(s.getStatus(), is(equalTo(UNDEFINED))),
            () -> assertFalse(s.isFailed())
        );
    }

    @Test
    void passed_undefined_skipped_is_undefined() {
        s.add(new Result(Status.PASSED, ZERO, null));
        s.add(new Result(Status.UNDEFINED, ZERO, null));
        s.add(new Result(Status.SKIPPED, ZERO, null));

        assertAll("Checking Scenario",
            () -> assertThat(s.getStatus(), is(equalTo(UNDEFINED))),
            () -> assertFalse(s.isFailed())
        );
    }

    @SuppressWarnings("deprecation")
    @Test
    void embeds_data() {
        byte[] data = new byte[]{1, 2, 3};
        s.embed(data, "bytes/foo");
        verify(bus).send(argThat(new EmbedEventMatcher(data, "bytes/foo")));
    }

    @Test
    void prints_output() {
        s.write("Hi");
        verify(bus).send(argThat(new WriteEventMatcher("Hi")));
    }

    @Test
    void failed_followed_by_pending_yields_failed_error() {
        Throwable failedError = mock(Throwable.class);
        Throwable pendingError = mock(Throwable.class);

        s.add(new Result(Status.FAILED, ZERO, failedError));
        s.add(new Result(Status.PENDING, ZERO, pendingError));

        assertThat(s.getError(), sameInstance(failedError));
    }

    @Test
    void pending_followed_by_failed_yields_failed_error() {
        Throwable pendingError = mock(Throwable.class);
        Throwable failedError = mock(Throwable.class);

        s.add(new Result(Status.PENDING, ZERO, pendingError));
        s.add(new Result(Status.FAILED, ZERO, failedError));

        assertThat(s.getError(), sameInstance(failedError));
    }

    private static final class EmbedEventMatcher implements ArgumentMatcher<EmbedEvent> {
        private byte[] data;
        private String mimeType;

        EmbedEventMatcher(byte[] data, String mimeType) {
            this.data = data;
            this.mimeType = mimeType;
        }

        @Override
        public boolean matches(EmbedEvent argument) {
            return (argument != null &&
                Arrays.equals(argument.getData(), data) && argument.getMimeType().equals(mimeType));
        }
    }

    private static final class WriteEventMatcher implements ArgumentMatcher<WriteEvent> {
        private String text;

        WriteEventMatcher(String text) {
            this.text = text;
        }

        @Override
        public boolean matches(WriteEvent argument) {
            return (argument != null && argument.getText().equals(text));
        }
    }

}
