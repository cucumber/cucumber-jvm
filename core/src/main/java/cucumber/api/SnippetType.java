package cucumber.api;

import cucumber.runtime.CucumberException;
import cucumber.runtime.snippets.CamelCaseFunctionNameSanitizer;
import cucumber.runtime.snippets.FunctionNameSanitizer;
import cucumber.runtime.snippets.UnderscoreFunctionNameSanitizer;

public enum SnippetType {
    UNDERSCORE("underscore", new UnderscoreFunctionNameSanitizer()),
    CAMELCASE("camelcase", new CamelCaseFunctionNameSanitizer());

    private final String name;
    private final FunctionNameSanitizer functionNameSanitizer;

    SnippetType(String name, FunctionNameSanitizer functionNameSanitizer) {
        this.name = name;
        this.functionNameSanitizer = functionNameSanitizer;
    }

    public static SnippetType fromString(String name) {
        for (SnippetType snippetType : SnippetType.values()) {
            if (name.equalsIgnoreCase(snippetType.name)) {
                return snippetType;
            }
        }
        throw new CucumberException(String.format("Unrecognized SnippetType %s", name));
    }

    public FunctionNameSanitizer getFunctionNameSanitizer() {
        return functionNameSanitizer;
    }
}
