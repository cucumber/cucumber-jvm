package cucumber.api.event;

import cucumber.api.TestCase;

public final class CommentAfterStepTextEvent extends TestCaseEvent {
    
    final TestCase testCase;
    final String after;
    final String stepText;

    public CommentAfterStepTextEvent(Long timeStamp, long timeStampMillis, TestCase testCase, String after, String stepText) {
    	super(timeStamp, timeStampMillis, testCase);
        this.testCase = testCase;
        this.after = after;
        this.stepText = stepText;
    }

    public String getAfter() {
        return after;
    }
    
    public String getStepText() {
        return stepText;
    }
    
}
