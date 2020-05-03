package io.cucumber.core.runtime;

import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.options.RuntimeOptions;
import io.cucumber.core.options.RuntimeOptionsBuilder;
import io.cucumber.plugin.event.Result;
import io.cucumber.plugin.event.Status;
import io.cucumber.plugin.event.TestCase;
import io.cucumber.plugin.event.TestCaseFinished;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import static java.time.Duration.ZERO;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.mock;

class ExitStatusTest {

    private final static Instant ANY_INSTANT = Instant.ofEpochMilli(1234567890);

    private EventBus bus;
    private ExitStatus exitStatus;

    @Test
    void should_pass_if_no_features_are_found() {
        createStrictRuntime();
        assertThat(exitStatus.exitStatus(), is(equalTo((byte) 0x0)));
    }

    private void createStrictRuntime() {
        createExitStatus(new RuntimeOptionsBuilder().build());
    }

    private void createExitStatus(RuntimeOptions runtimeOptions) {
        this.bus = new TimeServiceEventBus(Clock.systemUTC(), UUID::randomUUID);
        exitStatus = new ExitStatus(runtimeOptions);
        exitStatus.setEventPublisher(bus);
    }

    @Test
    void wip_with_ambiguous_scenarios() {
        createStrictWipRuntime();
        bus.send(testCaseFinishedWithStatus(Status.AMBIGUOUS));

        assertThat(exitStatus.exitStatus(), is(equalTo((byte) 0x0)));
    }

    private void createStrictWipRuntime() {
        createExitStatus(new RuntimeOptionsBuilder().setWip(true).build());
    }

    private TestCaseFinished testCaseFinishedWithStatus(Status resultStatus) {
        return new TestCaseFinished(ANY_INSTANT, mock(TestCase.class), new Result(resultStatus, ZERO, null));
    }

    @Test
    void wip_with_failed_failed_scenarios() {
        createStrictWipRuntime();
        bus.send(testCaseFinishedWithStatus(Status.FAILED));
        bus.send(testCaseFinishedWithStatus(Status.FAILED));

        assertThat(exitStatus.exitStatus(), is(equalTo((byte) 0x0)));
    }

    @Test
    void wip_with_failed_passed_scenarios() {
        createStrictWipRuntime();
        bus.send(testCaseFinishedWithStatus(Status.PASSED));
        bus.send(testCaseFinishedWithStatus(Status.FAILED));

        assertThat(exitStatus.exitStatus(), is(equalTo((byte) 0x1)));
    }

    @Test
    void wip_with_failed_scenarios() {
        createStrictWipRuntime();
        bus.send(testCaseFinishedWithStatus(Status.FAILED));

        assertThat(exitStatus.exitStatus(), is(equalTo((byte) 0x0)));
    }

    @Test
    void wip_with_passed_failed_scenarios() {
        createStrictWipRuntime();
        bus.send(testCaseFinishedWithStatus(Status.PASSED));
        bus.send(testCaseFinishedWithStatus(Status.FAILED));

        assertThat(exitStatus.exitStatus(), is(equalTo((byte) 0x1)));
    }

    @Test
    void wip_with_passed_scenarios() {
        createStrictWipRuntime();
        bus.send(testCaseFinishedWithStatus(Status.PASSED));

        assertThat(exitStatus.exitStatus(), is(equalTo((byte) 0x1)));
    }

    @Test
    void wip_with_pending_scenarios() {
        createStrictWipRuntime();
        bus.send(testCaseFinishedWithStatus(Status.PENDING));

        assertThat(exitStatus.exitStatus(), is(equalTo((byte) 0x0)));
    }

    @Test
    void wip_with_skipped_scenarios() {
        createNonStrictWipExitStatus();
        bus.send(testCaseFinishedWithStatus(Status.SKIPPED));

        assertThat(exitStatus.exitStatus(), is(equalTo((byte) 0x0)));
    }

    private void createNonStrictWipExitStatus() {
        createExitStatus(new RuntimeOptionsBuilder().setWip(true).build());
    }

    @Test
    void wip_with_undefined_scenarios() {
        createStrictWipRuntime();
        bus.send(testCaseFinishedWithStatus(Status.UNDEFINED));
        assertThat(exitStatus.exitStatus(), is(equalTo((byte) 0x0)));
    }

    @Test
    void with_ambiguous_scenarios() {
        createStrictRuntime();
        bus.send(testCaseFinishedWithStatus(Status.AMBIGUOUS));

        assertThat(exitStatus.exitStatus(), is(equalTo((byte) 0x1)));
    }

    @Test
    void with_failed_failed_scenarios() {
        createStrictRuntime();
        bus.send(testCaseFinishedWithStatus(Status.FAILED));
        bus.send(testCaseFinishedWithStatus(Status.FAILED));

        assertThat(exitStatus.exitStatus(), is(equalTo((byte) 0x1)));
    }

    @Test
    void with_failed_passed_scenarios() {
        createStrictRuntime();
        bus.send(testCaseFinishedWithStatus(Status.FAILED));
        bus.send(testCaseFinishedWithStatus(Status.PASSED));

        assertThat(exitStatus.exitStatus(), is(equalTo((byte) 0x1)));
    }

    @Test
    void with_failed_scenarios() {
        createStrictRuntime();
        bus.send(testCaseFinishedWithStatus(Status.FAILED));

        assertThat(exitStatus.exitStatus(), is(equalTo((byte) 0x1)));
    }

    @Test
    void with_passed_failed_scenarios() {
        createStrictRuntime();
        bus.send(testCaseFinishedWithStatus(Status.PASSED));
        bus.send(testCaseFinishedWithStatus(Status.FAILED));

        assertThat(exitStatus.exitStatus(), is(equalTo((byte) 0x1)));
    }

    @Test
    void with_passed_passed_scenarios() {
        createStrictRuntime();
        bus.send(testCaseFinishedWithStatus(Status.PASSED));
        bus.send(testCaseFinishedWithStatus(Status.PASSED));

        assertThat(exitStatus.exitStatus(), is(equalTo((byte) 0x0)));
    }

    @Test
    void with_passed_scenarios() {
        createStrictRuntime();
        bus.send(testCaseFinishedWithStatus(Status.PASSED));

        assertThat(exitStatus.exitStatus(), is(equalTo((byte) 0x0)));
    }

    @Test
    void with_pending_scenarios() {
        createStrictRuntime();
        bus.send(testCaseFinishedWithStatus(Status.PENDING));

        assertThat(exitStatus.exitStatus(), is(equalTo((byte) 0x1)));
    }

    @Test
    void with_skipped_scenarios() {
        createStrictRuntime();
        bus.send(testCaseFinishedWithStatus(Status.SKIPPED));

        assertThat(exitStatus.exitStatus(), is(equalTo((byte) 0x0)));
    }

    @Test
    void with_undefined_scenarios() {
        createStrictRuntime();
        bus.send(testCaseFinishedWithStatus(Status.UNDEFINED));
        assertThat(exitStatus.exitStatus(), is(equalTo((byte) 0x1)));
    }

}
