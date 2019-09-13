package io.cucumber.core.snippets;

import io.cucumber.plugin.event.DataTableArgument;
import io.cucumber.core.backend.Snippet;
import io.cucumber.core.feature.CucumberStep;
import io.cucumber.core.feature.DocStringArgument;
import io.cucumber.cucumberexpressions.CucumberExpressionGenerator;
import io.cucumber.cucumberexpressions.GeneratedExpression;
import io.cucumber.cucumberexpressions.ParameterType;
import io.cucumber.cucumberexpressions.ParameterTypeRegistry;
import io.cucumber.datatable.DataTable;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public final class SnippetGenerator {
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

    public List<String> getSnippet(CucumberStep step, SnippetType snippetType) {
        List<GeneratedExpression> generatedExpressions = generator.generateExpressions(step.getText());
        List<String> snippets = new ArrayList<>(generatedExpressions.size());
        FunctionNameGenerator functionNameGenerator = new FunctionNameGenerator(snippetType.joiner());
        for (GeneratedExpression expression : generatedExpressions) {
            snippets.add(snippet.template().format(new String[]{
                    sanitize(step.getKeyWord()),
                    snippet.escapePattern(expression.getSource()),
                    functionName(expression.getSource(), functionNameGenerator),
                    snippet.arguments(arguments(step, expression.getParameterNames(), expression.getParameterTypes())),
                    REGEXP_HINT,
                    tableHint(step)
                }
            ));
        }

        return snippets;
    }

    private String tableHint(CucumberStep step) {
        if (step.getArgument() == null) {
            return "";
        }

        if (step.getArgument() instanceof DataTableArgument) {
            return snippet.tableHint();
        }

        return "";
    }

    private static String sanitize(String keyWord) {
        return keyWord.replaceAll("[\\s',!]", "");
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


    private Map<String, Type> arguments(CucumberStep step, List<String> parameterNames, List<ParameterType<?>> parameterTypes) {
        Map<String, Type> arguments = new LinkedHashMap<>(parameterTypes.size() + 1);

        for (int i = 0; i < parameterTypes.size(); i++) {
            ParameterType<?> parameterType = parameterTypes.get(i);
            String parameterName = parameterNames.get(i);
            arguments.put(parameterName, parameterType.getType());
        }

        io.cucumber.core.feature.Argument arg = step.getArgument();
        if (arg == null) {
            return arguments;
        } else if (arg instanceof DocStringArgument) {
            arguments.put(parameterName("docString", parameterNames), String.class);
        } else if (arg instanceof io.cucumber.core.feature.DataTableArgument) {
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

    private ArgumentPattern[] argumentPatterns() {
        return DEFAULT_ARGUMENT_PATTERNS;
    }

}
