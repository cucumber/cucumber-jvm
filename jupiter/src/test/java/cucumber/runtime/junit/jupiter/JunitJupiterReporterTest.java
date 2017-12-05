package cucumber.runtime.junit.jupiter;

import cucumber.api.Result;
import cucumber.api.event.TestCaseFinished;
import cucumber.runner.EventBus;
import cucumber.runner.TimeService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class JunitJupiterReporterTest {

    @Test
    void reporter_should_report_passing_test_case() {
        EventBus eventBus = new EventBus(TimeService.SYSTEM);
        JunitJupiterReporter junitJupiterReporter = new JunitJupiterReporter(eventBus);
        Result result = new Result(Result.Type.PASSED, 0L, null);
        eventBus.send(new TestCaseFinished(0L, null, result));
        assertTrue(junitJupiterReporter.isOk(true));
    }

    @Test
    void reporter_should_report_passing_failing_test_case() {
        EventBus eventBus = new EventBus(TimeService.SYSTEM);
        JunitJupiterReporter junitJupiterReporter = new JunitJupiterReporter(eventBus);
        Exception throwable = new Exception();
        Result result = new Result(Result.Type.FAILED, 0L, throwable);
        eventBus.send(new TestCaseFinished(0L, null, result));
        assertFalse(junitJupiterReporter.isOk(true));
        assertEquals(junitJupiterReporter.getError(), throwable);
    }

}
