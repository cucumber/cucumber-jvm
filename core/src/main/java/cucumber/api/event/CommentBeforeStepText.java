package cucumber.api.event;

public class CommentBeforeStepText {
    String before;

    public CommentBeforeStepText(String before) {
        this.before = before;
        CommentEventSender.getInstance().sendCommentBeforeStepText(before);
    }
    
    
}
