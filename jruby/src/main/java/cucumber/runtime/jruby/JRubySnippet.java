package cucumber.runtime.jruby;

import cucumber.runtime.snippets.Snippet;

import java.util.List;

import static cucumber.runtime.snippets.SnippetGenerator.untypedArguments;

public class JRubySnippet implements Snippet {
    @Override
    public String template() {
        return "{0} '/'{1}'/' do{3}\n" +
                "  # {4}\n" +
                "  pending\n" +
                "end\n";
    }

    @Override
    public String tableHint() {
        return null;
    }

    @Override
    public String arguments(List<Class<?>> argumentTypes) {
        if (argumentTypes.isEmpty()) return "";
        return " |" + untypedArguments(argumentTypes.size()) + "|";
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
