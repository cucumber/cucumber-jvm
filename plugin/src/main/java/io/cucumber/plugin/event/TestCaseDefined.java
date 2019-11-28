package io.cucumber.plugin.event;

import org.apiguardian.api.API;

import java.time.Instant;
import java.util.Objects;

@API(status = API.Status.EXPERIMENTAL)
public final class TestCaseDefined extends TestCaseEvent {
    private final TestCase testCase;

    public TestCaseDefined(Instant timeInstant, TestCase testCase) {
        super(timeInstant, testCase);
        this.testCase = Objects.requireNonNull(testCase);
    }

    @Override
    public TestCase getTestCase() {
        return testCase;
    }
}
