package io.cucumber.core.snippets;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE)
public enum SnippetType {
    UNDERSCORE(new UnderscoreJoiner()),
    CAMELCASE(new CamelCaseJoiner());

    private final Joiner joiner;

    SnippetType(Joiner joiner) {
        this.joiner = joiner;
    }

    Joiner joiner() {
        return joiner;
    }
}
