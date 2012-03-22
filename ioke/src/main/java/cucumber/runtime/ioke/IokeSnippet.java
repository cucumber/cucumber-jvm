package cucumber.runtime.ioke;

import cucumber.runtime.snippets.Snippet;

import java.util.List;

public class IokeSnippet implements Snippet {

    @Override
    public String template() {
        return "{0}(#/{1}/,\n" +
                "  # {4}\n" +
                ")\n";
    }

    @Override
    public String tableHint() {
        return null;
    }

    @Override
    public String arguments(List<Class<?>> argumentTypes) {
        return null; // not used
    }

    @Override
    public String namedGroupStart() {
        return "{arg";
    }

    @Override
    public String namedGroupEnd() {
        return "}";
    }

    @Override
    public String escapePattern(String pattern) {
        return pattern;
    }
}
