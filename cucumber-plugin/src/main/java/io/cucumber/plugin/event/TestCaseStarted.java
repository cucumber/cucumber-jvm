package io.cucumber.plugin.event;

import org.apiguardian.api.API;

import java.time.Instant;
import java.util.Objects;

@API(status = API.Status.STABLE)
public final class TestCaseStarted extends TestCaseEvent {

    private final TestCase testCase;

    public TestCaseStarted(Instant timeInstant, TestCase testCase) {
        super(timeInstant, testCase);
        this.testCase = Objects.requireNonNull(testCase);
    }

    @Override
    public TestCase getTestCase() {
        return testCase;
    }

}
