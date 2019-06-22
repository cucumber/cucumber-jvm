package cucumber.runtime;


import cucumber.api.Result;
import cucumber.api.TestCase;
import cucumber.api.event.TestCaseFinished;
import cucumber.runner.EventBus;
import cucumber.runner.TimeService;
import cucumber.runner.TimeServiceEventBus;
import io.cucumber.core.options.RuntimeOptions;
import io.cucumber.core.options.CommandlineOptionsParser;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class ExitStatusTest {
    private final static long ANY_TIMESTAMP = 1234567890;

    private EventBus bus;
    private ExitStatus exitStatus;

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
        return new TestCaseFinished(ANY_TIMESTAMP, ANY_TIMESTAMP, mock(TestCase.class), new Result(resultStatus, 0L, null));
    }

    private void createExitStatus(String... runtimeArgs) {
        RuntimeOptions runtimeOptions = new CommandlineOptionsParser()
            .parse(runtimeArgs)
            .build();
        this.bus = new TimeServiceEventBus(TimeService.SYSTEM);
        exitStatus = new ExitStatus(runtimeOptions);
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