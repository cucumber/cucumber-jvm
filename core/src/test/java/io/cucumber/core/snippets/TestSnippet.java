package io.cucumber.core.snippets;

import java.lang.reflect.Type;
import java.util.Map;

public class TestSnippet implements Snippet {
    @Override
    public String template() {
        return "";
    }

    @Override
    public String tableHint() {
        return "";
    }

    @Override
    public String arguments(Map<String, Type> arguments) {
        return "";
    }

    @Override
    public String escapePattern(String pattern) {
        return "";
    }
}
