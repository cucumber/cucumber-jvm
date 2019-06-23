package io.cucumber.core.snippets;

public enum SnippetType {
    UNDERSCORE(new UnderscoreJoiner()),
    CAMELCASE(new CamelCaseJoiner());

    private final Joiner joiner;

    SnippetType(Joiner joiner) {
        this.joiner = joiner;
    }

    public Joiner joiner() {
        return joiner;
    }
}
