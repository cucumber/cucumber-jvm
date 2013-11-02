package cucumber.runtime.jruby;

import cucumber.runtime.snippets.Snippet;

import java.util.List;

public class JRubySnippet implements Snippet {
    @Override
    public String template() {
        return "{0} '/'{1}'/' do {3}\n" +
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
        StringBuilder sb = new StringBuilder(argumentTypes.isEmpty() ? "" : "|");
        for (int n = 0; n < argumentTypes.size(); n++) {
            if (n > 0) {
                sb.append(", ");
            }
            sb.append("arg").append(n + 1);
        }
        sb.append(argumentTypes.isEmpty() ? "" : "|");
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
        return pattern;
    }

}
