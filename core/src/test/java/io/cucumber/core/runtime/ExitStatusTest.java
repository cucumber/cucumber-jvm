package io.cucumber.core.runtime;

import io.cucumber.core.event.Result;
import io.cucumber.core.event.Status;
import io.cucumber.core.event.TestCase;
import io.cucumber.core.event.TestCaseFinished;
import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.options.CommandlineOptionsParser;
import io.cucumber.core.options.RuntimeOptions;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;

import static java.time.Duration.ZERO;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.mock;

public class ExitStatusTest {

    private final static Instant ANY_INSTANT = Instant.ofEpochMilli(1234567890);

    private EventBus bus;
    private Runtime.ExitStatus exitStatus;

    @Test
    public void non_strict_wip_with_ambiguous_scenarios() {
        createNonStrictWipExitStatus();
        bus.send(testCaseFinishedWithStatus(Status.AMBIGUOUS));

        assertThat(exitStatus.exitStatus(), is(equalTo((byte) 0x0)));
    }

    private void createNonStrictWipExitStatus() {
        createExitStatus("-g", "anything", "--wip");
    }

    private TestCaseFinished testCaseFinishedWithStatus(Status resultStatus) {
        return new TestCaseFinished(ANY_INSTANT, mock(TestCase.class), new Result(resultStatus, ZERO, null));
    }

    private void createExitStatus(String... runtimeArgs) {
        RuntimeOptions runtimeOptions = new CommandlineOptionsParser()
            .parse(runtimeArgs)
            .build();
        this.bus = new TimeServiceEventBus(Clock.systemUTC());
        exitStatus = new Runtime.ExitStatus(runtimeOptions);
        exitStatus.setEventPublisher(bus);
    }

    @Test
    public void non_strict_wip_with_failed_scenarios() {
        createNonStrictWipExitStatus();
        bus.send(testCaseFinishedWithStatus(Status.FAILED));

        assertThat(exitStatus.exitStatus(), is(equalTo((byte) 0x0)));
    }

    @Test
    public void non_strict_wip_with_passed_scenarios() {
        createNonStrictWipExitStatus();
        bus.send(testCaseFinishedWithStatus(Status.PASSED));

        assertThat(exitStatus.exitStatus(), is(equalTo((byte) 0x1)));
    }

    @Test
    public void non_strict_wip_with_pending_scenarios() {
        createNonStrictWipExitStatus();
        bus.send(testCaseFinishedWithStatus(Status.PENDING));

        assertThat(exitStatus.exitStatus(), is(equalTo((byte) 0x0)));
    }

    @Test
    public void non_strict_wip_with_skipped_scenarios() {
        createNonStrictWipExitStatus();
        bus.send(testCaseFinishedWithStatus(Status.SKIPPED));

        assertThat(exitStatus.exitStatus(), is(equalTo((byte) 0x0)));
    }

    @Test
    public void non_strict_wip_with_undefined_scenarios() {
        createNonStrictWipExitStatus();
        bus.send(testCaseFinishedWithStatus(Status.UNDEFINED));
        assertThat(exitStatus.exitStatus(), is(equalTo((byte) 0x0)));
    }

    @Test
    public void non_strict_with_ambiguous_scenarios() {
        createNonStrictExitStatus();
        bus.send(testCaseFinishedWithStatus(Status.AMBIGUOUS));

        assertThat(exitStatus.exitStatus(), is(equalTo((byte) 0x1)));
    }

    private void createNonStrictExitStatus() {
        createExitStatus("-g", "anything");
    }

    @Test
    public void non_strict_with_failed_scenarios() {
        createNonStrictExitStatus();
        bus.send(testCaseFinishedWithStatus(Status.FAILED));

        assertThat(exitStatus.exitStatus(), is(equalTo((byte) 0x1)));
    }

    @Test
    public void non_strict_with_passed_scenarios() {
        createNonStrictExitStatus();
        bus.send(testCaseFinishedWithStatus(Status.PASSED));

        assertThat(exitStatus.exitStatus(), is(equalTo((byte) 0x0)));
    }

    @Test
    public void non_strict_with_pending_scenarios() {
        createNonStrictExitStatus();
        bus.send(testCaseFinishedWithStatus(Status.PENDING));

        assertThat(exitStatus.exitStatus(), is(equalTo((byte) 0x0)));
    }

    @Test
    public void non_strict_with_skipped_scenarios() {
        createNonStrictExitStatus();
        bus.send(testCaseFinishedWithStatus(Status.SKIPPED));

        assertThat(exitStatus.exitStatus(), is(equalTo((byte) 0x0)));
    }

    @Test
    public void non_strict_with_undefined_scenarios() {
        createNonStrictExitStatus();
        bus.send(testCaseFinishedWithStatus(Status.UNDEFINED));
        assertThat(exitStatus.exitStatus(), is(equalTo((byte) 0x0)));
    }

    @Test
    public void should_pass_if_no_features_are_found() {
        createStrictRuntime();
        assertThat(exitStatus.exitStatus(), is(equalTo((byte) 0x0)));
    }

    @Test
    public void strict_wip_with_ambiguous_scenarios() {
        createStrictWipRuntime();
        bus.send(testCaseFinishedWithStatus(Status.AMBIGUOUS));

        assertThat(exitStatus.exitStatus(), is(equalTo((byte) 0x0)));
    }

    private void createStrictWipRuntime() {
        createExitStatus("-g", "anything", "--strict", "--wip");
    }

    @Test
    public void strict_wip_with_failed_failed_scenarios() {
        createStrictWipRuntime();
        bus.send(testCaseFinishedWithStatus(Status.FAILED));
        bus.send(testCaseFinishedWithStatus(Status.FAILED));

        assertThat(exitStatus.exitStatus(), is(equalTo((byte) 0x0)));
    }

    @Test
    public void strict_wip_with_failed_passed_scenarios() {
        createStrictWipRuntime();
        bus.send(testCaseFinishedWithStatus(Status.PASSED));
        bus.send(testCaseFinishedWithStatus(Status.FAILED));

        assertThat(exitStatus.exitStatus(), is(equalTo((byte) 0x1)));
    }

    @Test
    public void strict_wip_with_failed_scenarios() {
        createStrictWipRuntime();
        bus.send(testCaseFinishedWithStatus(Status.FAILED));

        assertThat(exitStatus.exitStatus(), is(equalTo((byte) 0x0)));
    }

    @Test
    public void strict_wip_with_passed_failed_scenarios() {
        createStrictWipRuntime();
        bus.send(testCaseFinishedWithStatus(Status.PASSED));
        bus.send(testCaseFinishedWithStatus(Status.FAILED));

        assertThat(exitStatus.exitStatus(), is(equalTo((byte) 0x1)));
    }

    @Test
    public void strict_wip_with_passed_scenarios() {
        createStrictWipRuntime();
        bus.send(testCaseFinishedWithStatus(Status.PASSED));

        assertThat(exitStatus.exitStatus(), is(equalTo((byte) 0x1)));
    }

    @Test
    public void strict_wip_with_pending_scenarios() {
        createStrictWipRuntime();
        bus.send(testCaseFinishedWithStatus(Status.PENDING));

        assertThat(exitStatus.exitStatus(), is(equalTo((byte) 0x0)));
    }

    @Test
    public void strict_wip_with_skipped_scenarios() {
        createNonStrictWipExitStatus();
        bus.send(testCaseFinishedWithStatus(Status.SKIPPED));

        assertThat(exitStatus.exitStatus(), is(equalTo((byte) 0x0)));
    }

    @Test
    public void strict_wip_with_undefined_scenarios() {
        createStrictWipRuntime();
        bus.send(testCaseFinishedWithStatus(Status.UNDEFINED));
        assertThat(exitStatus.exitStatus(), is(equalTo((byte) 0x0)));
    }

    @Test
    public void strict_with_ambiguous_scenarios() {
        createStrictRuntime();
        bus.send(testCaseFinishedWithStatus(Status.AMBIGUOUS));

        assertThat(exitStatus.exitStatus(), is(equalTo((byte) 0x1)));
    }

    private void createStrictRuntime() {
        createExitStatus("-g", "anything", "--strict");
    }

    @Test
    public void strict_with_failed_failed_scenarios() {
        createStrictRuntime();
        bus.send(testCaseFinishedWithStatus(Status.FAILED));
        bus.send(testCaseFinishedWithStatus(Status.FAILED));

        assertThat(exitStatus.exitStatus(), is(equalTo((byte) 0x1)));
    }

    @Test
    public void strict_with_failed_passed_scenarios() {
        createStrictRuntime();
        bus.send(testCaseFinishedWithStatus(Status.FAILED));
        bus.send(testCaseFinishedWithStatus(Status.PASSED));

        assertThat(exitStatus.exitStatus(), is(equalTo((byte) 0x1)));
    }

    @Test
    public void strict_with_failed_scenarios() {
        createStrictRuntime();
        bus.send(testCaseFinishedWithStatus(Status.FAILED));

        assertThat(exitStatus.exitStatus(), is(equalTo((byte) 0x1)));
    }

    @Test
    public void strict_with_passed_failed_scenarios() {
        createStrictRuntime();
        bus.send(testCaseFinishedWithStatus(Status.PASSED));
        bus.send(testCaseFinishedWithStatus(Status.FAILED));

        assertThat(exitStatus.exitStatus(), is(equalTo((byte) 0x1)));
    }

    @Test
    public void strict_with_passed_passed_scenarios() {
        createStrictRuntime();
        bus.send(testCaseFinishedWithStatus(Status.PASSED));
        bus.send(testCaseFinishedWithStatus(Status.PASSED));

        assertThat(exitStatus.exitStatus(), is(equalTo((byte) 0x0)));
    }

    @Test
    public void strict_with_passed_scenarios() {
        createStrictRuntime();
        bus.send(testCaseFinishedWithStatus(Status.PASSED));

        assertThat(exitStatus.exitStatus(), is(equalTo((byte) 0x0)));
    }

    @Test
    public void strict_with_pending_scenarios() {
        createStrictRuntime();
        bus.send(testCaseFinishedWithStatus(Status.PENDING));

        assertThat(exitStatus.exitStatus(), is(equalTo((byte) 0x1)));
    }

    @Test
    public void strict_with_skipped_scenarios() {
        createNonStrictExitStatus();
        bus.send(testCaseFinishedWithStatus(Status.SKIPPED));

        assertThat(exitStatus.exitStatus(), is(equalTo((byte) 0x0)));
    }

    @Test
    public void strict_with_undefined_scenarios() {
        createStrictRuntime();
        bus.send(testCaseFinishedWithStatus(Status.UNDEFINED));
        assertThat(exitStatus.exitStatus(), is(equalTo((byte) 0x1)));
    }

}
