package io.cucumber.core.api.event;

public final class TestCaseStarted extends TestCaseEvent {
    public final TestCase testCase;
    private final long elapsedTimiMillis; 

    public TestCaseStarted(Long timeStamp, Long elapsedTimiMillis, TestCase testCase) {
        super(timeStamp, testCase);
        this.testCase = testCase;
        this.elapsedTimiMillis = elapsedTimiMillis;
    }

    public long getElapsedTimiMillis() {
        return elapsedTimiMillis;
    }

}
