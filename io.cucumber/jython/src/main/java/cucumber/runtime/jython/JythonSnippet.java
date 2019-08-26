package cucumber.runtime.jython;

import cucumber.runtime.snippets.Snippet;

import java.util.List;

import static cucumber.runtime.snippets.SnippetGenerator.untypedArguments;

public class JythonSnippet implements Snippet {

    @Override
    public String template() {
        return "@{0}(''{1}'')\n" +
                "def {2}({3}):\n" +
                "  # {4}\n" +
                "{5}  raise(PendingException())\n" +
                "";
    }

    @Override
    public String tableHint() {
        return "  # The last argument is a List of List of String\n";
    }

    @Override
    public String arguments(List<Class<?>> argumentTypes) {
        String args = untypedArguments(argumentTypes);
        return args.equals("") ? "self" : "self, " + args;
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
