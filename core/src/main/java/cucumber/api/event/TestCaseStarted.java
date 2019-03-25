package cucumber.api.event;

import cucumber.api.TestCase;

public final class TestCaseStarted extends TestCaseEvent {
    public final TestCase testCase;
    private final long timeStampMillis; 
    
    @Deprecated
    public TestCaseStarted(Long timeStamp, TestCase testCase) {
       this(timeStamp, 0L, testCase);
    }

    public TestCaseStarted(Long timeStamp, Long timeStampMillis, TestCase testCase) {
        super(timeStamp, testCase);
        this.testCase = testCase;
        this.timeStampMillis = timeStampMillis;
    }

    public long getTimeStampMillis() {
        return timeStampMillis;
    }
}
