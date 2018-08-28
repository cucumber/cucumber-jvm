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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static java.text.MessageFormat.format;

public class SnippetGenerator {
    @SuppressWarnings("RegExpRedundantEscape") // Android can't parse unescaped braces.
    private static final ArgumentPattern[] DEFAULT_ARGUMENT_PATTERNS = new ArgumentPattern[]{
        new ArgumentPattern(Pattern.compile("\\{.*?\\}"))
    };

    private static final String REGEXP_HINT = "Write code here that turns the phrase above into concrete actions";

    private final Snippet snippet;
    private final CucumberExpressionGenerator generator;

    public SnippetGenerator(Snippet snippet, ParameterTypeRegistry parameterTypeRegistry) {
        this.snippet = snippet;
        this.generator = new CucumberExpressionGenerator(parameterTypeRegistry);
    }

    public List<String> getSnippet(PickleStep step, String keyword, FunctionNameGenerator functionNameGenerator) {
        List<GeneratedExpression> generatedExpressions = generator.generateExpressions(step.getText());
        List<String> snippets = new ArrayList<>(generatedExpressions.size());

        for (GeneratedExpression expression : generatedExpressions) {
            snippets.add(format(
                snippet.template(),
                keyword,
                snippet.escapePattern(expression.getSource()),
                functionName(expression.getSource(), functionNameGenerator),
                snippet.arguments(arguments(step, expression.getParameterNames(), expression.getParameterTypes())),
                REGEXP_HINT,
                !step.getArgument().isEmpty() && step.getArgument().get(0) instanceof PickleTable ? snippet.tableHint() : ""
            ));
        }

        return snippets;
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


    private Map<String, Type> arguments(PickleStep step, List<String> parameterNames, List<ParameterType<?>> parameterTypes) {
        Map<String, Type> arguments = new LinkedHashMap<String, Type>(parameterTypes.size() + 1);

        for (int i = 0; i < parameterTypes.size(); i++) {
            ParameterType<?> parameterType = parameterTypes.get(i);
            String parameterName = parameterNames.get(i);
            arguments.put(parameterName, parameterType.getType());
        }

        if (step.getArgument().isEmpty()) {
            return arguments;
        }

        Argument arg = step.getArgument().get(0);
        if (arg instanceof PickleString) {
            arguments.put(parameterName("docString", parameterNames), String.class);
        }
        if (arg instanceof PickleTable) {
            arguments.put(parameterName("dataTable", parameterNames), DataTable.class);
        }

        return arguments;
    }

    private String parameterName(String name, List<String> parameterNames) {
        if (!parameterNames.contains(name)) {
            return name;
        }

        for (int i = 1; ; i++) {
            if (!parameterNames.contains(name + i)) {
                return name + i;
            }
        }
    }

    ArgumentPattern[] argumentPatterns() {
        return DEFAULT_ARGUMENT_PATTERNS;
    }

}
