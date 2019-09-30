package cucumber.api.event;

import cucumber.api.TestCase;
import cucumber.runner.EventBus;

public class CommentEventSender {
    private EventBus bus;
    private TestCase testCase;
    private String stepText;
    
    private static final CommentEventSender instance = new CommentEventSender();
    
    private CommentEventSender() {
    }
    
    public void setBus(EventBus bus) {
        this.bus = bus;
    }
    
    public void setTestCase(TestCase testCase) {
        this.testCase = testCase;
    }
    
    public void setStepText(String stepText) {
        this.stepText = stepText;
    }
    
    public void sendCommentBeforeStepText(String before) {
        bus.send(new CommentBeforeStepTextEvent(bus.getTime(), bus.getTimeMillis(), this.testCase, before, this.stepText));
    }
    public void sendCommentAfterStepText(String after) {
        bus.send(new CommentAfterStepTextEvent(bus.getTime(), bus.getTimeMillis(), this.testCase, after, this.stepText));
    }
    
    public static final CommentEventSender getInstance() 
    {
        return instance;
    }
}
