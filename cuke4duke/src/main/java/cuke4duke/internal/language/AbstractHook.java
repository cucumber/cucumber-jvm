package cuke4duke.internal.language;

import java.util.List;

public abstract class AbstractHook implements Hook  {
    private String[] tagNames;
    private List<List<String>> tagNameLists;

    public AbstractHook(String[] tagNames) {
        this.tagNames = tagNames;
    }

    public final String[] tag_names() {
        return tagNames;
    }

    public final void setTagNameLists(List<List<String>> tagNameLists) {
        this.tagNameLists = tagNameLists;
    }

    public final List<List<String>> getTagNameLists() {
        return tagNameLists;
    }
}
