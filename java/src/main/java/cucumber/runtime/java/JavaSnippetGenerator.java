package cucumber.runtime.java;

import cucumber.runtime.ParameterPatternExchanger;
import cucumber.runtime.SnippetGenerator;
import gherkin.formatter.model.Step;

import java.util.List;
import java.util.regex.Pattern;

public class JavaSnippetGenerator extends SnippetGenerator {
    private static final ParameterPatternExchanger[] JavaParameterPatterns = new ParameterPatternExchanger[] {
        ParameterPatternExchanger.ExchangeMatchsWithPattern(Pattern.compile("\"([^\"]*)\""), String.class),
        ParameterPatternExchanger.ExchangeMatchesWithReplacement(Pattern.compile("(\\d+)"), "(\\\\d+)",Integer.TYPE)
  };
    
    public JavaSnippetGenerator(Step step) {
        super(step);
    }

    @Override
    protected String pattern(String name) {
        return super.pattern(name).replaceAll("\"", "\\\\\"");
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
    protected ParameterPatternExchanger[] parameterPatterns() {
        return JavaParameterPatterns;
    }
}
