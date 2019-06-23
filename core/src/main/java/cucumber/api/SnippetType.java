package cucumber.api;

import cucumber.runtime.CucumberException;
import cucumber.runtime.snippets.CamelCaseConcatenator;
import cucumber.runtime.snippets.Concatenator;
import cucumber.runtime.snippets.FunctionNameGenerator;
import cucumber.runtime.snippets.UnderscoreConcatenator;

/**
 * @deprecated use {@link io.cucumber.junit.CucumberOptions} or {@link io.cucumber.testng.CucumberOptions} instead.
 */
@Deprecated
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
        throw new CucumberException(String.format("Unrecognized SnippetType %s", name));
    }

    public FunctionNameGenerator getFunctionNameGenerator() {
        return new FunctionNameGenerator(concatenator);
    }
}
