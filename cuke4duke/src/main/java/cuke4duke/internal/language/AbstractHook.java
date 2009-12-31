package cuke4duke.internal.language;

public abstract class AbstractHook implements Hook  {
    private final String[] tagExpressions;

    public AbstractHook(String[] tagExpressions) {
        this.tagExpressions = tagExpressions;
    }

    public final String[] tag_expressions() {
        return tagExpressions;
    }
}
