package io.cucumber.core.runtime;


import io.cucumber.core.api.event.Result;
import io.cucumber.core.api.event.TestCase;
import io.cucumber.core.api.event.TestCaseFinished;
import io.cucumber.core.event.EventBus;
import io.cucumber.core.io.MultiLoader;
import io.cucumber.core.options.Env;
import io.cucumber.core.runner.TimeServiceEventBus;
import io.cucumber.core.options.RuntimeOptions;
import org.junit.Test;

import static java.time.Duration.ZERO;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.time.Clock;
import java.time.Instant;

public class ExitStatusTest {
    private final static Instant ANY_INSTANT = Instant.ofEpochMilli(1234567890);

    private EventBus bus;
    private Runtime.ExitStatus exitStatus;

    @Test
    public void non_strict_wip_with_ambiguous_scenarios() {
        createNonStrictWipExitStatus();
        bus.send(testCaseFinishedWithStatus(Result.Type.AMBIGUOUS));

        assertEquals(0x0, exitStatus.exitStatus());
    }

    private void createNonStrictWipExitStatus() {
        createExitStatus("-g", "anything", "--wip");
    }

    private TestCaseFinished testCaseFinishedWithStatus(Result.Type resultStatus) {
        return new TestCaseFinished(ANY_INSTANT, mock(TestCase.class), new Result(resultStatus, ZERO, null));
    }

    private void createExitStatus(String... runtimeArgs) {
        RuntimeOptions runtimeOptions = new RuntimeOptions(new MultiLoader(RuntimeOptions.class.getClassLoader()), Env.INSTANCE, asList(runtimeArgs));
        this.bus = new TimeServiceEventBus(Clock.systemUTC());
        exitStatus = new Runtime.ExitStatus(runtimeOptions);
        exitStatus.setEventPublisher(bus);
    }

    @Test
    public void non_strict_wip_with_failed_scenarios() {
        createNonStrictWipExitStatus();
        bus.send(testCaseFinishedWithStatus(Result.Type.FAILED));

        assertEquals(0x0, exitStatus.exitStatus());
    }

    @Test
    public void non_strict_wip_with_passed_scenarios() {
        createNonStrictWipExitStatus();
        bus.send(testCaseFinishedWithStatus(Result.Type.PASSED));

        assertEquals(0x1, exitStatus.exitStatus());
    }

    @Test
    public void non_strict_wip_with_pending_scenarios() {
        createNonStrictWipExitStatus();
        bus.send(testCaseFinishedWithStatus(Result.Type.PENDING));

        assertEquals(0x0, exitStatus.exitStatus());
    }

    @Test
    public void non_strict_wip_with_skipped_scenarios() {
        createNonStrictWipExitStatus();
        bus.send(testCaseFinishedWithStatus(Result.Type.SKIPPED));

        assertEquals(0x0, exitStatus.exitStatus());
    }

    @Test
    public void non_strict_wip_with_undefined_scenarios() {
        createNonStrictWipExitStatus();
        bus.send(testCaseFinishedWithStatus(Result.Type.UNDEFINED));
        assertEquals(0x0, exitStatus.exitStatus());
    }

    @Test
    public void non_strict_with_ambiguous_scenarios() {
        createNonStrictExitStatus();
        bus.send(testCaseFinishedWithStatus(Result.Type.AMBIGUOUS));

        assertEquals(0x1, exitStatus.exitStatus());
    }

    private void createNonStrictExitStatus() {
        createExitStatus("-g", "anything");
    }

    @Test
    public void non_strict_with_failed_scenarios() {
        createNonStrictExitStatus();
        bus.send(testCaseFinishedWithStatus(Result.Type.FAILED));

        assertEquals(0x1, exitStatus.exitStatus());
    }

    @Test
    public void non_strict_with_passed_scenarios() {
        createNonStrictExitStatus();
        bus.send(testCaseFinishedWithStatus(Result.Type.PASSED));

        assertEquals(0x0, exitStatus.exitStatus());
    }

    @Test
    public void non_strict_with_pending_scenarios() {
        createNonStrictExitStatus();
        bus.send(testCaseFinishedWithStatus(Result.Type.PENDING));

        assertEquals(0x0, exitStatus.exitStatus());
    }

    @Test
    public void non_strict_with_skipped_scenarios() {
        createNonStrictExitStatus();
        bus.send(testCaseFinishedWithStatus(Result.Type.SKIPPED));

        assertEquals(0x0, exitStatus.exitStatus());
    }

    @Test
    public void non_strict_with_undefined_scenarios() {
        createNonStrictExitStatus();
        bus.send(testCaseFinishedWithStatus(Result.Type.UNDEFINED));
        assertEquals(0x0, exitStatus.exitStatus());
    }

    @Test
    public void should_pass_if_no_features_are_found() {
        createStrictRuntime();
        assertEquals(0x0, exitStatus.exitStatus());
    }

    @Test
    public void strict_wip_with_ambiguous_scenarios() {
        createStrictWipRuntime();
        bus.send(testCaseFinishedWithStatus(Result.Type.AMBIGUOUS));

        assertEquals(0x0, exitStatus.exitStatus());
    }

    private void createStrictWipRuntime() {
        createExitStatus("-g", "anything", "--strict", "--wip");
    }

    @Test
    public void strict_wip_with_failed_failed_scenarios() {
        createStrictWipRuntime();
        bus.send(testCaseFinishedWithStatus(Result.Type.FAILED));
        bus.send(testCaseFinishedWithStatus(Result.Type.FAILED));

        assertEquals(0x0, exitStatus.exitStatus());
    }

    @Test
    public void strict_wip_with_failed_passed_scenarios() {
        createStrictWipRuntime();
        bus.send(testCaseFinishedWithStatus(Result.Type.PASSED));
        bus.send(testCaseFinishedWithStatus(Result.Type.FAILED));

        assertEquals(0x1, exitStatus.exitStatus());
    }

    @Test
    public void strict_wip_with_failed_scenarios() {
        createStrictWipRuntime();
        bus.send(testCaseFinishedWithStatus(Result.Type.FAILED));

        assertEquals(0x0, exitStatus.exitStatus());
    }

    @Test
    public void strict_wip_with_passed_failed_scenarios() {
        createStrictWipRuntime();
        bus.send(testCaseFinishedWithStatus(Result.Type.PASSED));
        bus.send(testCaseFinishedWithStatus(Result.Type.FAILED));

        assertEquals(0x1, exitStatus.exitStatus());
    }

    @Test
    public void strict_wip_with_passed_scenarios() {
        createStrictWipRuntime();
        bus.send(testCaseFinishedWithStatus(Result.Type.PASSED));

        assertEquals(0x1, exitStatus.exitStatus());
    }

    @Test
    public void strict_wip_with_pending_scenarios() {
        createStrictWipRuntime();
        bus.send(testCaseFinishedWithStatus(Result.Type.PENDING));

        assertEquals(0x0, exitStatus.exitStatus());
    }

    @Test
    public void strict_wip_with_skipped_scenarios() {
        createNonStrictWipExitStatus();
        bus.send(testCaseFinishedWithStatus(Result.Type.SKIPPED));

        assertEquals(0x0, exitStatus.exitStatus());
    }

    @Test
    public void strict_wip_with_undefined_scenarios() {
        createStrictWipRuntime();
        bus.send(testCaseFinishedWithStatus(Result.Type.UNDEFINED));
        assertEquals(0x0, exitStatus.exitStatus());
    }

    @Test
    public void strict_with_ambiguous_scenarios() {
        createStrictRuntime();
        bus.send(testCaseFinishedWithStatus(Result.Type.AMBIGUOUS));

        assertEquals(0x1, exitStatus.exitStatus());
    }

    private void createStrictRuntime() {
        createExitStatus("-g", "anything", "--strict");
    }

    @Test
    public void strict_with_failed_failed_scenarios() {
        createStrictRuntime();
        bus.send(testCaseFinishedWithStatus(Result.Type.FAILED));
        bus.send(testCaseFinishedWithStatus(Result.Type.FAILED));

        assertEquals(0x1, exitStatus.exitStatus());
    }

    @Test
    public void strict_with_failed_passed_scenarios() {
        createStrictRuntime();
        bus.send(testCaseFinishedWithStatus(Result.Type.FAILED));
        bus.send(testCaseFinishedWithStatus(Result.Type.PASSED));

        assertEquals(0x1, exitStatus.exitStatus());
    }

    @Test
    public void strict_with_failed_scenarios() {
        createStrictRuntime();
        bus.send(testCaseFinishedWithStatus(Result.Type.FAILED));

        assertEquals(0x1, exitStatus.exitStatus());
    }

    @Test
    public void strict_with_passed_failed_scenarios() {
        createStrictRuntime();
        bus.send(testCaseFinishedWithStatus(Result.Type.PASSED));
        bus.send(testCaseFinishedWithStatus(Result.Type.FAILED));

        assertEquals(0x1, exitStatus.exitStatus());
    }

    @Test
    public void strict_with_passed_passed_scenarios() {
        createStrictRuntime();
        bus.send(testCaseFinishedWithStatus(Result.Type.PASSED));
        bus.send(testCaseFinishedWithStatus(Result.Type.PASSED));

        assertEquals(0x0, exitStatus.exitStatus());
    }

    @Test
    public void strict_with_passed_scenarios() {
        createStrictRuntime();
        bus.send(testCaseFinishedWithStatus(Result.Type.PASSED));

        assertEquals(0x0, exitStatus.exitStatus());
    }

    @Test
    public void strict_with_pending_scenarios() {
        createStrictRuntime();
        bus.send(testCaseFinishedWithStatus(Result.Type.PENDING));

        assertEquals(0x1, exitStatus.exitStatus());
    }

    @Test
    public void strict_with_skipped_scenarios() {
        createNonStrictExitStatus();
        bus.send(testCaseFinishedWithStatus(Result.Type.SKIPPED));

        assertEquals(0x0, exitStatus.exitStatus());
    }

    @Test
    public void strict_with_undefined_scenarios() {
        createStrictRuntime();
        bus.send(testCaseFinishedWithStatus(Result.Type.UNDEFINED));
        assertEquals(0x1, exitStatus.exitStatus());
    }

}