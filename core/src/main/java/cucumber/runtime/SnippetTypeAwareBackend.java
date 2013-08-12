package cucumber.runtime;

public interface SnippetTypeAwareBackend extends Backend {

    public void setSnippetType(SnippetType type);

}
