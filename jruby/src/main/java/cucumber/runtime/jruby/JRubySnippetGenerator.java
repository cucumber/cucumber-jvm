package cucumber.runtime.jruby;

import cucumber.runtime.snippets.SnippetGenerator;
import gherkin.formatter.model.Step;

import java.util.List;

public class JRubySnippetGenerator extends SnippetGenerator {
    protected JRubySnippetGenerator(Step step) {
        super(step);
    }

    @Override
    protected String template() {
        return "{0} '/'{1}'/' do {3}\n" +
                "  # {4}\n" +
                "  pending\n" +
                "end\n";
    }

    @Override
    protected String arguments(List<Class<?>> argumentTypes) {
        StringBuilder sb = new StringBuilder(argumentTypes.isEmpty() ? "" : "|");
        for (int n = 0; n < argumentTypes.size(); n++) {
            if (n > 1) {
                sb.append(", ");
            }
            sb.append("arg").append(n + 1);
        }
        sb.append(argumentTypes.isEmpty() ? "" : "|");
        return sb.toString();
    }
}
