package io.cucumber.core.api.event;

import java.time.Instant;

public final class TestCaseFinished extends TestCaseEvent {
    public final Result result;
    public final TestCase testCase;

    @Deprecated
    public TestCaseFinished(Long timeStamp, TestCase testCase, Result result) {
        this(timeStamp, 0, testCase, result);
    }

    @Deprecated
    public TestCaseFinished(Long timeStamp, long timeStampMillis, TestCase testCase, Result result) {
        super(timeStamp, timeStampMillis, testCase);
        this.testCase = testCase;
        this.result = result;
    }

    public TestCaseFinished(Instant timeInstant, TestCase testCase, Result result) {
      super(timeInstant, testCase);
      this.testCase = testCase;
      this.result = result;
  }
}
