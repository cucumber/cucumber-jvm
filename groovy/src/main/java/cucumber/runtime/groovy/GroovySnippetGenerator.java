package cucumber.runtime.groovy;

import cucumber.runtime.snippets.SnippetGenerator;
import gherkin.formatter.model.Step;

import java.util.List;

public class GroovySnippetGenerator extends SnippetGenerator {
    public GroovySnippetGenerator(Step step) {
        super(step, true);
    }

    @Override
    protected String patternFor(String stepName) {
        return super.patternFor(stepName).replaceAll("\"", "\\\\\"");
    }

    @Override
    protected String template() {
        return "{0}(~\"{1}\") '{' {3}->\n" +
                "    // {4}\n" +
                "'}'\n";
    }

    @Override
    protected String arguments(List<Class<?>> argumentTypes) {
        StringBuilder sb = new StringBuilder();
        int n = 1;
        for (Class<?> argType : argumentTypes) {
            if (n > 1) {
                sb.append(", ");
            }
            sb.append(argType.getSimpleName()).append(" ").append("arg").append(n++);
        }
        if (sb.length() > 0) {
            sb.append(" ");
        }
        return sb.toString();
    }
}
