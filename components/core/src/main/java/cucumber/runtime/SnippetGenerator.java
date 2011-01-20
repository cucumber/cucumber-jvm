package cucumber.runtime;

import gherkin.I18n;
import gherkin.formatter.model.Step;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Base class for generating snippets.
 */
public abstract class SnippetGenerator {
    private static final Pattern[] ARG_PATTERNS = new Pattern[]{
            Pattern.compile("\"([^\"]*)\""),
            Pattern.compile("(\\d+)")
    };
    private static final Class<?>[] ARG_TYPES = new Class<?>[]{
            String.class,
            Integer.TYPE
    };
    private static final String HINT = "Express the Regexp above with the code you wish you had";

    private final Step step;

    protected SnippetGenerator(Step step) {
        this.step = step;
    }

    public String getSnippet() {
        String keyword = I18n.codeKeywordFor(step.getKeyword());
        String pattern = pattern(step.getName());
        String methodName = function(step.getName());
        String arguments = arguments(argumentTypes(step.getName()));

        return MessageFormat.format(template(), keyword, pattern, methodName, arguments, HINT);
    }

    protected abstract String template();

    protected abstract String arguments(List<Class<?>> argymentTypes);

    private String function(String name) {
        String f = name;
        for (Pattern argPattern : ARG_PATTERNS) {
            Matcher m = argPattern.matcher(f);
            f = m.replaceAll(" ");
        }
        f = f.replaceAll("\\s+", "_");
        return f;
    }

    protected String pattern(String name) {
        String snippetPattern = name;
        for (Pattern argPattern : ARG_PATTERNS) {
            Matcher m = argPattern.matcher(snippetPattern);
            snippetPattern = m.replaceAll(Matcher.quoteReplacement(argPattern.pattern()));
        }
        return "^" + snippetPattern + "$";
    }

    private List<Class<?>> argumentTypes(String name) {
        List<Class<?>> argTypes = new ArrayList<Class<?>>();
        Matcher[] matchers = new Matcher[ARG_TYPES.length];
        for (int i = 0; i < ARG_TYPES.length; i++) {
            matchers[i] = ARG_PATTERNS[i].matcher(name);
        }
        int pos = 0;
        while (true) {
            for (int i = 0; i < matchers.length; i++) {
                Matcher m = matchers[i].region(pos, name.length());
                if (m.lookingAt()) {
                    argTypes.add(ARG_TYPES[i]);
                }
            }
            if (pos++ == name.length()) {
                break;
            }
        }
        return argTypes;
    }
}
