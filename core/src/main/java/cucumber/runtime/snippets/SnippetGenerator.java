package cucumber.runtime.snippets;

import cucumber.api.DataTable;
import gherkin.pickles.Argument;
import gherkin.pickles.PickleStep;
import gherkin.pickles.PickleString;
import gherkin.pickles.PickleTable;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SnippetGenerator {
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
            Pattern.compile("\\]"),
            Pattern.compile("\\?"),
            Pattern.compile("\\*"),
            Pattern.compile("\\+"),
            Pattern.compile("\\."),
            Pattern.compile("\\^")
    };

    private static final String REGEXP_HINT = "Write code here that turns the phrase above into concrete actions";

    private final Snippet snippet;

    public SnippetGenerator(Snippet snippet) {
        this.snippet = snippet;
    }

    public String getSnippet(PickleStep step, String keyword, FunctionNameGenerator functionNameGenerator) {
        return MessageFormat.format(
                snippet.template(),
                keyword,
                snippet.escapePattern(patternFor(step.getText())),
                functionName(step.getText(), functionNameGenerator),
                snippet.arguments(argumentTypes(step)),
                REGEXP_HINT,
                !step.getArgument().isEmpty() && step.getArgument().get(0) instanceof PickleTable ? snippet.tableHint() : ""
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

    private String functionName(String sentence, FunctionNameGenerator functionNameGenerator) {
        if(functionNameGenerator == null) {
            return null;
        }
        for (ArgumentPattern argumentPattern : argumentPatterns()) {
            sentence = argumentPattern.replaceMatchesWithSpace(sentence);
        }
        return functionNameGenerator.generateFunctionName(sentence);
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


    private List<Class<?>> argumentTypes(PickleStep step) {
        String name = step.getText();
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
        if (!step.getArgument().isEmpty()) {
            Argument arg = step.getArgument().get(0);
            if (arg instanceof PickleString) {
                argTypes.add(String.class);
            }
            if (arg instanceof PickleTable) {
                argTypes.add(DataTable.class);
            }
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
