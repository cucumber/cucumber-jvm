package cucumber.runtime.rhino;

import cucumber.runtime.snippets.Snippet;

import java.util.List;

import static cucumber.runtime.snippets.SnippetGenerator.untypedArguments;

public class JavaScriptSnippet implements Snippet {
    @Override
    public String template() {
        return "{0}(/{1}/, function({3}) '{'\n" +
                "  // {4}\n" +
                "  throw new Packages.cucumber.api.PendingException();\n" +
                "'}');\n";
    }

    @Override
    public String tableHint() {
        return null;
    }

    @Override
    public String arguments(List<Class<?>> argumentTypes) {
        return untypedArguments(argumentTypes);
    }

    @Override
    public String namedGroupStart() {
        return null;
    }

    @Override
    public String namedGroupEnd() {
        return null;
    }

    @Override
    public String escapePattern(String pattern) {
        return pattern;
    }
}
