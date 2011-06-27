package cucumber.runtime.ioke;

import cucumber.runtime.SnippetGenerator;
import gherkin.formatter.model.Step;

import java.util.List;

public class IokeSnippetGenerator extends SnippetGenerator {
    protected IokeSnippetGenerator(Step step) {
        super(step, "{arg", "}");
    }

    @Override
    protected String template() {
        return "{0}(#/{1}/,\n" +
                "  # {4}\n" +
                ")\n";
    }

    @Override
    protected String arguments(List<Class<?>> argymentTypes) {
        return null; // not used
    }
}
