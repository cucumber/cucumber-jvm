package cucumber.runtime;

public enum SnippetType {
    UNDERSCORE("underscore"),
    CAMELCASE("camelcase");

    private String type;

    SnippetType(String type) {
        this.type = type;
    }

    public static SnippetType fromString(String type) {
        if (type == null) {
            throw new CucumberException("Cannot pass null as SnippetType");
        }
        for (SnippetType snippetType : SnippetType.values()) {
            if (type.equalsIgnoreCase(snippetType.type)) {
                return snippetType;
            }
        }
        throw new CucumberException(String.format("Unrecognized SnippetType %s", type));
    }

    public static SnippetType getDefault() {
        return UNDERSCORE;
    }
}
