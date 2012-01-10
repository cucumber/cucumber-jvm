package cucumber.runtime.clojure;

import cucumber.runtime.snippets.SnippetGenerator;
import gherkin.formatter.model.Step;

import java.util.List;

public class ClojureSnippetGenerator extends SnippetGenerator {
    protected ClojureSnippetGenerator(Step step) {
        super(step);
    }

    @Override
    protected String patternFor(String stepName) {
        return super.patternFor(stepName).replaceAll("\"", "\\\\\"");
    }

    @Override
    protected String template() {
        return "({0} #\"{1}\"\n" +
                "  (fn [{3}]\n" +
                "    \" {4}\n" + // TODO: The " should be a ', but that causes a propblem with MessageFormat escaping {4}. Need to read up on MessageFormat docs.
                "    ))\n";
    }

    @Override
    protected String arguments(List<Class<?>> argumentTypes) {
        return untypedArguments(argumentTypes);
    }
}
