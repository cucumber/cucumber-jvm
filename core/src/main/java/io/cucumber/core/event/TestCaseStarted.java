package io.cucumber.core.event;

import java.time.Instant;

public final class TestCaseStarted extends TestCaseEvent {
    public final TestCase testCase;

    public TestCaseStarted(Instant timeInstant, TestCase testCase) {
      super(timeInstant, testCase);
      this.testCase = testCase;
  }

}
