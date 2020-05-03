package io.cucumber.core.snippets;

import io.cucumber.core.backend.Snippet;

import java.lang.reflect.Type;
import java.text.MessageFormat;
import java.util.Map;

public class TestSnippet implements Snippet {

    @Override
    public MessageFormat template() {
        return new MessageFormat("");
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
