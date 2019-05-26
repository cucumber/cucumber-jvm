package io.cucumber.core.api.event;

import java.time.Instant;

public final class TestCaseFinished extends TestCaseEvent {
    public final Result result;
    public final TestCase testCase;

    public TestCaseFinished(Instant timeInstant, TestCase testCase, Result result) {
      super(timeInstant, testCase);
      this.testCase = testCase;
      this.result = result;
  }
}
