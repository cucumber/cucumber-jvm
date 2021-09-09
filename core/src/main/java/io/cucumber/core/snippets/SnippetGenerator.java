package io.cucumber.core.snippets;

import io.cucumber.core.backend.Snippet;
import io.cucumber.core.gherkin.Step;
import io.cucumber.cucumberexpressions.CucumberExpressionGenerator;
import io.cucumber.cucumberexpressions.GeneratedExpression;
import io.cucumber.cucumberexpressions.ParameterType;
import io.cucumber.cucumberexpressions.ParameterTypeRegistry;
import io.cucumber.datatable.DataTable;
import io.cucumber.plugin.event.DataTableArgument;
import io.cucumber.plugin.event.DocStringArgument;
import io.cucumber.plugin.event.StepArgument;

import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.cucumber.core.snippets.SnippetType.CAMELCASE;

public final class SnippetGenerator {

    // Android can't parse unescaped braces.
    @SuppressWarnings("RegExpRedundantEscape")
    private static final ArgumentPattern DEFAULT_ARGUMENT_PATTERN = new ArgumentPattern(Pattern.compile("\\{.*?\\}"));

    private static final String REGEXP_HINT = "Write code here that turns the phrase above into concrete actions";

    private final Snippet snippet;
    private final CucumberExpressionGenerator generator;

    public SnippetGenerator(Snippet snippet, ParameterTypeRegistry parameterTypeRegistry) {
        this.snippet = snippet;
        this.generator = new CucumberExpressionGenerator(parameterTypeRegistry);
    }

    public List<String> getSnippet(Step step, SnippetType snippetType) {
        List<GeneratedExpression> generatedExpressions = generator.generateExpressions(step.getText());
        IdentifierGenerator functionNameGenerator = new IdentifierGenerator(snippetType.joiner());
        IdentifierGenerator parameterNameGenerator = new IdentifierGenerator(CAMELCASE.joiner());
        return generatedExpressions.stream()
                .map(expression -> createSnippet(step, functionNameGenerator, parameterNameGenerator, expression))
                .collect(Collectors.toList());
    }

    private String createSnippet(
            Step step, IdentifierGenerator functionNameGenerator,
            IdentifierGenerator parameterNameGenerator, GeneratedExpression expression
    ) {
        String keyword = step.getType().isGivenWhenThen() ? step.getKeyword() : step.getPreviousGivenWhenThenKeyword();
        String source = expression.getSource();
        String functionName = functionName(source, functionNameGenerator);
        List<String> parameterNames = toParameterNames(expression, parameterNameGenerator);
        Map<String, Type> arguments = arguments(step, parameterNames, expression.getParameterTypes());
        return snippet.template().format(new String[] {
                sanitize(keyword),
                snippet.escapePattern(source),
                functionName,
                snippet.arguments(arguments),
                REGEXP_HINT,
                tableHint(step)
        });
    }

    private List<String> toParameterNames(GeneratedExpression expression, IdentifierGenerator parameterNameGenerator) {
        List<String> parameterNames = expression.getParameterNames();
        return parameterNames.stream()
                .map(parameterNameGenerator::generate)
                .collect(Collectors.toList());
    }

    private static String sanitize(String keyWord) {
        return keyWord.replaceAll("[\\s',!]", "");
    }

    private String functionName(String sentence, IdentifierGenerator functionNameGenerator) {
        return Stream.of(sentence)
                .map(DEFAULT_ARGUMENT_PATTERN::replaceMatchesWithSpace)
                .map(functionNameGenerator::generate)
                .filter(s -> !s.isEmpty())
                .findFirst()
                .orElseGet(() -> functionNameGenerator.generate(sentence));
    }

    private Map<String, Type> arguments(Step step, List<String> parameterNames, List<ParameterType<?>> parameterTypes) {
        Map<String, Type> arguments = new LinkedHashMap<>(parameterTypes.size() + 1);

        for (int i = 0; i < parameterTypes.size(); i++) {
            ParameterType<?> parameterType = parameterTypes.get(i);
            String parameterName = parameterNames.get(i);
            arguments.put(parameterName, parameterType.getType());
        }

        StepArgument arg = step.getArgument();
        if (arg == null) {
            return arguments;
        } else if (arg instanceof DocStringArgument) {
            arguments.put(parameterName("docString", parameterNames), String.class);
        } else if (arg instanceof DataTableArgument) {
            arguments.put(parameterName("dataTable", parameterNames), DataTable.class);
        }

        return arguments;
    }

    private String tableHint(Step step) {
        if (step.getArgument() == null) {
            return "";
        }

        if (step.getArgument() instanceof DataTableArgument) {
            return snippet.tableHint();
        }

        return "";
    }

    private String parameterName(String name, List<String> parameterNames) {
        if (!parameterNames.contains(name)) {
            return name;
        }

        for (int i = 1;; i++) {
            if (!parameterNames.contains(name + i)) {
                return name + i;
            }
        }
    }

}
