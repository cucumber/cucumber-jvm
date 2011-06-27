package cucumber.runtime.java;

import cucumber.runtime.SnippetGenerator;
import gherkin.formatter.model.Step;

import java.util.List;

public class JavaSnippetGenerator extends SnippetGenerator {
    public JavaSnippetGenerator(Step step) {
        super(step);
    }

    @Override
    protected String pattern(String name) {
        return super.pattern(name).replaceAll("\"", "\\\\\"");
    }

    @Override
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

    @Override
    protected String template() {
        return "@{0}(\"{1}\")\n" +
                "public void {2}({3}) '{'\n" +
                "    // {4}\n" +
                "'}'\n";
    }
}
