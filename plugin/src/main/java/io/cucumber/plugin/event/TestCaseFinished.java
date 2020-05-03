package io.cucumber.plugin.event;

import org.apiguardian.api.API;

import java.time.Instant;
import java.util.Objects;

@API(status = API.Status.STABLE)
public final class TestCaseFinished extends TestCaseEvent {

    private final Result result;
    private final TestCase testCase;

    public TestCaseFinished(Instant timeInstant, TestCase testCase, Result result) {
        super(timeInstant, testCase);
        this.testCase = Objects.requireNonNull(testCase);
        this.result = Objects.requireNonNull(result);
    }

    public Result getResult() {
        return result;
    }

    @Override
    public TestCase getTestCase() {
        return testCase;
    }

}
