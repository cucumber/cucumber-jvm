package cucumber.api.event;

public class CommentAfterStepText {
    String after;

    public CommentAfterStepText(String after) {
        this.after = after;
        CommentEventSender.getInstance().sendCommentAfterStepText(after);
    }
    
    
}
