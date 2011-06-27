package cucumber.runtime.javascript;

import cucumber.runtime.SnippetGenerator;
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
        StringBuilder sb = new StringBuilder ();
        for (int n = 0; n < argumentTypes.size(); n++) {
            if (n > 1) {
                sb.append(", ");
            }
            sb.append("arg").append(n+1);
        }
        return sb.toString();
    }
}
