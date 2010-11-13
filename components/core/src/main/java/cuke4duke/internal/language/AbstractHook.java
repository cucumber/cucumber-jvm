package cuke4duke.internal.language;

import java.util.List;

public abstract class AbstractHook implements Hook {
    private final List<String> tagExpressions;

    public AbstractHook(List<String> tagExpressions) {
        this.tagExpressions = tagExpressions;
    }

    public final List<String> getTagExpressions() {
        return tagExpressions;
    }
}
