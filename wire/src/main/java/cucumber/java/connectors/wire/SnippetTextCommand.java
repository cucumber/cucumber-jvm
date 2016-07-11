package cucumber.java.connectors.wire;

import cucumber.java.CukeEngine;

public class SnippetTextCommand implements WireCommand {
    private String step_keyword;
    private String step_name;
    private String multiline_arg_class;

    public SnippetTextCommand(
            String step_keyword,
            String step_name,
            String multiline_arg_class) {
        this.step_keyword = step_keyword;
        this.step_name = step_name;
        this.multiline_arg_class = multiline_arg_class;
    }

    public WireResponse run(CukeEngine engine) {
        return new SnippetTextResponse(engine.snippetText(step_keyword, step_name, multiline_arg_class));
    }
}
