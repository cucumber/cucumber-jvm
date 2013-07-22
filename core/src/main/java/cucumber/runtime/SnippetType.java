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
            throw new IllegalArgumentException();
        }
        for (SnippetType snippetType : SnippetType.values()) {
            if (type.equalsIgnoreCase(snippetType.type)) {
                return snippetType;
            }
        }
        throw new IllegalArgumentException();
    }

    public static SnippetType getDefault() {
        return UNDERSCORE;
    }
}
