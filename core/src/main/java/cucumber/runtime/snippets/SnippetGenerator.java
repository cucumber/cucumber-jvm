package cucumber.runtime.snippets;

import cucumber.table.DataTable;
import gherkin.I18n;
import gherkin.formatter.model.Step;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SnippetGenerator {
    private static final ArgumentPattern[] DEFAULT_ARGUMENT_PATTERNS = new ArgumentPattern[]{
            new ArgumentPattern(Pattern.compile("\"([^\"]*)\""), String.class),
            new ArgumentPattern(Pattern.compile("(\\d+)"), Integer.TYPE)
    };
    private static final Pattern GROUP_PATTERN = Pattern.compile("\\(");
    private static final Pattern[] ESCAPE_PATTERNS = new Pattern[]{
            Pattern.compile("\\$"),
            Pattern.compile("\\("),
            Pattern.compile("\\)"),
            Pattern.compile("\\["),
            Pattern.compile("\\]")
    };

    private static final String REGEXP_HINT = "Express the Regexp above with the code you wish you had";
    private static final Character SUBST = '_';

    private final Snippet snippet;

    public SnippetGenerator(Snippet snippet) {
        this.snippet = snippet;
    }

    public String getSnippet(Step step) {
        return MessageFormat.format(
                snippet.template(),
                I18n.codeKeywordFor(step.getKeyword()),
                snippet.escapePattern(patternFor(step.getName())),
                functionName(step.getName()),
                snippet.arguments(argumentTypes(step)),
                REGEXP_HINT,
                step.getRows() == null ? "" : snippet.tableHint()
        );
    }

    String patternFor(String stepName) {
        String pattern = stepName;
        for (Pattern escapePattern : ESCAPE_PATTERNS) {
            Matcher m = escapePattern.matcher(pattern);
            String replacement = Matcher.quoteReplacement(escapePattern.toString());
            pattern = m.replaceAll(replacement);
        }
        for (ArgumentPattern argumentPattern : argumentPatterns()) {
            pattern = argumentPattern.replaceMatchesWithGroups(pattern);
        }
        if (snippet.namedGroupStart() != null) {
            pattern = withNamedGroups(pattern);
        }

        return "^" + pattern + "$";
    }

    private String functionName(String name) {
        String functionName = name;
        for (ArgumentPattern argumentPattern : argumentPatterns()) {
            functionName = argumentPattern.replaceMatchesWithSpace(functionName);
        }
        functionName = sanitizeFunctionName(functionName);
        return functionName;
    }

    String sanitizeFunctionName(String functionName) {
        StringBuilder sanitized = new StringBuilder();

        String trimmedFunctionName = functionName.trim();

        sanitized.append(Character.isJavaIdentifierStart(trimmedFunctionName.charAt(0)) ? trimmedFunctionName.charAt(0) : SUBST);
        for (int i = 1; i < trimmedFunctionName.length(); i++) {
            if (Character.isJavaIdentifierPart(trimmedFunctionName.charAt(i))) {
                sanitized.append(trimmedFunctionName.charAt(i));
            } else if (sanitized.charAt(sanitized.length() - 1) != SUBST && i != trimmedFunctionName.length() - 1) {
                sanitized.append(SUBST);
            }
        }
        return sanitized.toString();
    }

    private String withNamedGroups(String snippetPattern) {
        Matcher m = GROUP_PATTERN.matcher(snippetPattern);

        StringBuffer sb = new StringBuffer();
        int n = 1;
        while (m.find()) {
            m.appendReplacement(sb, "(" + snippet.namedGroupStart() + n++ + snippet.namedGroupEnd());
        }
        m.appendTail(sb);

        return sb.toString();
    }


    private List<Class<?>> argumentTypes(Step step) {
        String name = step.getName();
        List<Class<?>> argTypes = new ArrayList<Class<?>>();
        Matcher[] matchers = new Matcher[argumentPatterns().length];
        for (int i = 0; i < argumentPatterns().length; i++) {
            matchers[i] = argumentPatterns()[i].pattern().matcher(name);
        }
        int pos = 0;
        while (true) {
            int matchedLength = 1;

            for (int i = 0; i < matchers.length; i++) {
                Matcher m = matchers[i].region(pos, name.length());
                if (m.lookingAt()) {
                    Class<?> typeForSignature = argumentPatterns()[i].type();
                    argTypes.add(typeForSignature);

                    matchedLength = m.group().length();
                    break;
                }
            }

            pos += matchedLength;

            if (pos == name.length()) {
                break;
            }
        }
        if (step.getDocString() != null) {
            argTypes.add(String.class);
        }
        if (step.getRows() != null) {
            argTypes.add(DataTable.class);
        }
        return argTypes;
    }

    ArgumentPattern[] argumentPatterns() {
        return DEFAULT_ARGUMENT_PATTERNS;
    }

    public static String untypedArguments(List<Class<?>> argumentTypes) {
        StringBuilder sb = new StringBuilder();
        for (int n = 0; n < argumentTypes.size(); n++) {
            if (n > 0) {
                sb.append(", ");
            }
            sb.append("arg").append(n + 1);
        }
        return sb.toString();
    }
}