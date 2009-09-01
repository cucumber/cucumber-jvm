package cuke4duke.internal.language;

import org.jruby.RubyArray;
import java.util.List;
import cuke4duke.internal.JRuby;

public abstract class AbstractHook implements Hook  {
    protected final List<String> tagNames;

    public AbstractHook(List<String> tagNames) {
        this.tagNames = tagNames;
    }

    public RubyArray tag_names() {
        RubyArray arr = RubyArray.newArray(JRuby.getRuntime());
        for(String tag : tagNames) {
            String trimmed = tag.trim();
            if(!trimmed.equals("")) {
                arr.add(tag);
            }
        }
        return arr;
    }
}
