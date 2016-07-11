package cucumber.java.connectors.wire;

public class SnippetTextResponse implements WireResponse {
    private String stepSnippet;

    public SnippetTextResponse(String  stepSnippet) {
        this.stepSnippet = stepSnippet;
    }

    public String getStepSnippet() { return stepSnippet; }
}
