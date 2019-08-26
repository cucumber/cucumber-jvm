package cucumber.runtime.gosu;

import cucumber.runtime.snippets.Snippet;

import java.util.List;

public class GosuSnippet implements Snippet {
    @Override
    public String template() {
        return "Dsl.{0}(''{1}'', \\ {3}-> '{'\n" +
                "    // {4}\n" +
                "    throw new PendingException()\n" +
                "'}')\n";
    }

    @Override
    public String tableHint() {
        return null;
    }

    @Override
    public String arguments(List<Class<?>> argumentTypes) {
        StringBuilder sb = new StringBuilder();
        int n = 1;
        for (Class<?> argType : argumentTypes) {
            if (n > 1) {
                sb.append(", ");
            }
            sb.append("arg").append(n++).append(" : ").append(argType.getSimpleName()).append(" ");
        }
        if (sb.length() > 0) {
            sb.append(" ");
        }
        return sb.toString();
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
        return pattern.replace("\\", "\\\\");
    }
}
