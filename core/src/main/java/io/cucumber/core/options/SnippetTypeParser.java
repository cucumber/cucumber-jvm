package io.cucumber.core.options;

import io.cucumber.core.snippets.SnippetType;

public final class SnippetTypeParser {

    private SnippetTypeParser() {

    }

    public static SnippetType parseSnippetType(String nextArg) {
        SnippetType underscore;
        if ("underscore".equals(nextArg)) {
            underscore = SnippetType.UNDERSCORE;
        } else if ("camelcase".equals(nextArg)) {
            underscore = SnippetType.CAMELCASE;
        } else {
            throw new IllegalArgumentException("Unrecognized SnippetType " + nextArg);
        }
        return underscore;
    }

}
