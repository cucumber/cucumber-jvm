package cucumber.runtime.java;

import cucumber.runtime.SnippetGenerator;
import gherkin.formatter.model.Step;

import java.util.List;

public class JavaSnippetGenerator extends SnippetGenerator {
    public JavaSnippetGenerator(Step step) {
        super(step);
    }

    protected String arguments(List<Class<?>> argumentTypes) {
        StringBuilder sb = new StringBuilder ();
        int n = 1;
        for (Class<?> argType : argumentTypes) {
            if (n > 1) {
                sb.append(", ");
            }
            sb.append(argType.getSimpleName()).append(" ").append("arg").append(n++);
        }
        return sb.toString();
    }

    protected String template() {
        return "@{0}(\"{1}\")\n" +
                "public void {2}({3}) '{'\n" +
                "    // {4}\n" +
                "'}'\n";
    }
}
