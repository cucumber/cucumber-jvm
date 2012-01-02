package cucumber.runtime.ioke;

import cucumber.runtime.snippets.SnippetGenerator;
import gherkin.formatter.model.Step;

import java.util.List;

public class IokeSnippetGenerator extends SnippetGenerator {
    protected IokeSnippetGenerator(Step step) {
        super(step, "{arg", "}", false);
    }

    @Override
    protected String template() {
        return "{0}(#/{1}/,\n" +
                "  # {4}\n" +
                ")\n";
    }

    @Override
    protected String arguments(List<Class<?>> argumentTypes) {
        return null; // not used
    }
}
