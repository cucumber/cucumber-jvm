package cucumber.runtime.clojure;

import cucumber.runtime.snippets.Snippet;

import java.util.List;

import static cucumber.runtime.snippets.SnippetGenerator.untypedArguments;

/**
 * This class is only here to test clojure snippets in isolation.
 * Keep the template in sync with clj.clj
 */
public class ClojureSnippet implements Snippet {
    @Override
    public String template() {
        return "({0} #\"{1}\" [{3}]\n" +
                "  (comment  {4}  )\n" +
                "  (throw (cucumber.runtime.PendingException.)))\n";
    }

    @Override
    public String tableHint() {
        return null;
    }

    @Override
    public String arguments(List<Class<?>> argumentTypes) {
        return untypedArguments(argumentTypes).replaceAll(",", "");
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
        return pattern.replaceAll("\"", "\\\\\"");
    }
}
