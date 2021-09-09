package io.cucumber.core.snippets;

public enum SnippetType {
    UNDERSCORE(new SnakeCaseJoiner()),
    CAMELCASE(new CamelCaseJoiner());

    private final Joiner joiner;

    SnippetType(Joiner joiner) {
        this.joiner = joiner;
    }

    Joiner joiner() {
        return joiner;
    }
}
