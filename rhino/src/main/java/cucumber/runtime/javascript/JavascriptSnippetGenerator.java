package cucumber.runtime.javascript;

import cucumber.runtime.snippets.SnippetGenerator;
import gherkin.formatter.model.Step;

import java.util.List;

public class JavascriptSnippetGenerator extends SnippetGenerator {
    public JavascriptSnippetGenerator(Step step) {
        super(step);
    }

    @Override
    protected String template() {
        return "{0}(/{1}/, function({3}) '{'\n" +
                "  // {4}\n" +
                "'}');\n";
    }

    @Override
    protected String arguments(List<Class<?>> argumentTypes) {
        return untypedArguments(argumentTypes);
    }
}
