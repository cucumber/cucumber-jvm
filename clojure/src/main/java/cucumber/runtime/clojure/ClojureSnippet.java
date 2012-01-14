package cucumber.runtime.clojure;

import cucumber.runtime.snippets.Snippet;

import java.util.List;

import static cucumber.runtime.snippets.SnippetGenerator.untypedArguments;

public class ClojureSnippet implements Snippet {
    @Override
    public String template() {
        return "({0} #\"{1}\"\n" +
                "  (fn [{3}]\n" +
                "    \" {4}\n" + // TODO: The " should be a ', but that causes a propblem with MessageFormat escaping {4}. Need to read up on MessageFormat docs.
                "    ))\n";
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
