package cucumber.runtime.snippets;

import io.cucumber.cucumberexpressions.GeneratedExpression;
import io.cucumber.cucumberexpressions.ParameterType;
import io.cucumber.datatable.DataTable;
import gherkin.pickles.Argument;
import gherkin.pickles.PickleStep;
import gherkin.pickles.PickleString;
import gherkin.pickles.PickleTable;
import io.cucumber.cucumberexpressions.CucumberExpressionGenerator;
import io.cucumber.cucumberexpressions.ParameterTypeRegistry;

import java.lang.reflect.Type;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class SnippetGenerator {
    private static final ArgumentPattern[] DEFAULT_ARGUMENT_PATTERNS = new ArgumentPattern[]{
        new ArgumentPattern(Pattern.compile("\\{.*?}")),
    };

    private static final String REGEXP_HINT = "Write code here that turns the phrase above into concrete actions";

    private final Snippet snippet;
    private final CucumberExpressionGenerator generator;

    public SnippetGenerator(Snippet snippet, ParameterTypeRegistry parameterTypeRegistry) {
        this.snippet = snippet;
        this.generator = new CucumberExpressionGenerator(parameterTypeRegistry);
    }

    public String getSnippet(PickleStep step, String keyword, FunctionNameGenerator functionNameGenerator) {
        List<GeneratedExpression> expressions = generator.generateExpressions(step.getText());
        GeneratedExpression expression = expressions.get(0);

        return MessageFormat.format(
            snippet.template(),
            keyword,
            snippet.escapePattern(expression.getSource()),
            functionName(expression.getSource(), functionNameGenerator),
            snippet.arguments(argumentTypes(step, expression.getParameterTypes())),
            REGEXP_HINT,
            !step.getArgument().isEmpty() && step.getArgument().get(0) instanceof PickleTable ? snippet.tableHint() : ""
        );
    }

    private String functionName(String sentence, FunctionNameGenerator functionNameGenerator) {
        if (functionNameGenerator == null) {
            return null;
        }
        for (ArgumentPattern argumentPattern : argumentPatterns()) {
            sentence = argumentPattern.replaceMatchesWithSpace(sentence);
        }
        return functionNameGenerator.generateFunctionName(sentence);
    }


    private List<Type> argumentTypes(PickleStep step, List<ParameterType<?>> parameterTypes) {
        List<Type> types = new ArrayList<Type>(parameterTypes.size() + 1);

        for (ParameterType<?> parameterType : parameterTypes) {
            types.add(parameterType.getType());
        }

        if (step.getArgument().isEmpty()) {
            return types;
        }
        Argument arg = step.getArgument().get(0);
        if (arg instanceof PickleString) {
            types.add(String.class);
        }
        if (arg instanceof PickleTable) {
            types.add(DataTable.class);
        }

        return types;
    }

    ArgumentPattern[] argumentPatterns() {
        return DEFAULT_ARGUMENT_PATTERNS;
    }

}
