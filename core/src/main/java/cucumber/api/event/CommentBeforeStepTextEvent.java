package cucumber.api.event;

import cucumber.api.TestCase;

public final class CommentBeforeStepTextEvent extends TestCaseEvent {
    
    final TestCase testCase;
    final String before;
    final String stepText;

    public CommentBeforeStepTextEvent(Long timeStamp, long timeStampMillis, TestCase testCase, String before, String stepText) {
    	super(timeStamp, timeStampMillis, testCase);
        this.testCase = testCase;
        this.before = before;
        this.stepText = stepText;
    }

    public String getBefore() {
        return before;
    }
    
    public String getStepText() {
        return stepText;
    }
    
}
