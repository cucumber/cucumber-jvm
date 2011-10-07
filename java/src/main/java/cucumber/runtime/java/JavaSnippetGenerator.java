package cucumber.runtime.java;

import cucumber.runtime.snippets.SnippetGenerator;
import gherkin.formatter.model.Step;

import java.util.List;

public class JavaSnippetGenerator extends SnippetGenerator {
    private static final Character SUBST = '_';

    public JavaSnippetGenerator(Step step) {
        super(step);
    }

    @Override
    protected String patternFor(String stepName) {
        return super.patternFor(stepName).replaceAll("\"", "\\\\\"");
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
        return sb.toString();
    }

    @Override
    protected String template() {
        return "@{0}(\"{1}\")\n" +
                "public void {2}({3}) '{'\n" +
                "    // {4}\n" +
                "'}'\n";
    }
    
    @Override
    protected String sanitizeFunctionName(String functionName) {
        StringBuilder sanitized = new StringBuilder();
        sanitized.append(Character.isJavaIdentifierStart(functionName.charAt(0)) ? functionName.charAt(0) : SUBST);
        for (int i = 1; i < functionName.length(); i++) {
            if (Character.isJavaIdentifierPart(functionName.charAt(i))) {
                sanitized.append(functionName.charAt(i));
            } else if (sanitized.charAt(sanitized.length() - 1) != SUBST && i != functionName.length() - 1) {
                sanitized.append(SUBST);
            }
        }
        return sanitized.toString();
    }
}
