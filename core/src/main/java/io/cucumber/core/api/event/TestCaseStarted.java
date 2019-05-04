package io.cucumber.core.api.event;

import java.time.Instant;

public final class TestCaseStarted extends TestCaseEvent {
    public final TestCase testCase;

    @Deprecated
    public TestCaseStarted(Long timeStamp, TestCase testCase) {
       this(timeStamp, 0L, testCase);
    }

    @Deprecated
    public TestCaseStarted(Long timeStamp, long timeStampMillis, TestCase testCase) {
        super(timeStamp, timeStampMillis, testCase);
        this.testCase = testCase;
    }
    
    public TestCaseStarted(Instant timeInstant, TestCase testCase) {
      super(timeInstant, testCase);
      this.testCase = testCase;
  }

}
