package cucumber.api;

import io.cucumber.core.snippets.CamelCaseConcatenator;
import io.cucumber.core.snippets.Concatenator;
import io.cucumber.core.snippets.FunctionNameGenerator;
import io.cucumber.core.snippets.UnderscoreConcatenator;

public enum SnippetType {
    UNDERSCORE("underscore", new UnderscoreConcatenator()),
    CAMELCASE("camelcase", new CamelCaseConcatenator());

    private final String name;
    private final Concatenator concatenator;

    SnippetType(String name, Concatenator concatenator) {
        this.name = name;
        this.concatenator = concatenator;
    }

    public static SnippetType fromString(String name) {
        for (SnippetType snippetType : SnippetType.values()) {
            if (name.equalsIgnoreCase(snippetType.name)) {
                return snippetType;
            }
        }
        throw new IllegalArgumentException(String.format("Unrecognized SnippetType %s", name));
    }

    public FunctionNameGenerator getFunctionNameGenerator() {
        return new FunctionNameGenerator(concatenator);
    }
}
