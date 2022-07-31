package io.cucumber.plugin.event;

import org.apiguardian.api.API;

import java.time.Instant;
import java.util.Objects;

@API(status = API.Status.STABLE)
public abstract class TestCaseEvent extends TimeStampedEvent {

    private final TestCase testCase;

    TestCaseEvent(Instant timeInstant, TestCase testCase) {
        super(timeInstant);
        this.testCase = Objects.requireNonNull(testCase);
    }

    public TestCase getTestCase() {
        return testCase;
    }

}
