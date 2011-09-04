package cucumber.runtime.jython;

import cucumber.runtime.SnippetGenerator;
import gherkin.formatter.model.Step;

import java.util.List;

public class JythonSnippetGenerator extends SnippetGenerator {
    protected JythonSnippetGenerator(Step step) {
        super(step);
    }

    @Override
    protected String template() {
        return "@{0}(''{1}'')\n" +
                "def {2}({3}):\n" +
                "  # {4}\n";
    }

    @Override
    protected String arguments(List<Class<?>> argumentTypes) {
        return untypedArguments(argumentTypes);
    }
}
