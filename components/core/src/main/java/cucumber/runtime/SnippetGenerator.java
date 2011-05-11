package cucumber.runtime;

import gherkin.I18n;
import gherkin.model.Step;

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
    private static final Pattern GROUP_PATTERN = Pattern.compile("\\(");
    private static final String HINT = "Express the Regexp above with the code you wish you had";

    private final Step step;
    private final String namedGroupStart;
    private final String namedGroupEnd;

    /**
     * Constructor for langauges that do not support named capture groups, such ash Java.
     *
     * @param step the step to generate snippet for.
     */
    protected SnippetGenerator(Step step) {
        this(step, null, null);
    }

    /**
     * Constructor for langauges that support named capture groups, such ash Ioke.
     *
     * @param step the step to generate snippet for.
     * @param namedGroupStart beginning of named group, for example "{arg".
     * @param namedGroupEnd end of named group, for example "}".
     */
    protected SnippetGenerator(Step step, String namedGroupStart, String namedGroupEnd) {
        this.step = step;
        this.namedGroupStart = namedGroupStart;
        this.namedGroupEnd = namedGroupEnd;
    }

    public String getSnippet() {
        return MessageFormat.format(template(), I18n.codeKeywordFor(step.getKeyword()), pattern(step.getName()), functionName(step.getName()), arguments(argumentTypes(step.getName())), HINT);
    }

    protected abstract String template();

    protected abstract String arguments(List<Class<?>> argymentTypes);

    protected String pattern(String name) {
        String snippetPattern = name;
        for (Pattern argPattern : ARG_PATTERNS) {
            Matcher m = argPattern.matcher(snippetPattern);
            snippetPattern = m.replaceAll(Matcher.quoteReplacement(argPattern.pattern()));
        }
        if (namedGroupStart != null) {
            snippetPattern = withNamedGroups(snippetPattern);
        }

        return "^" + snippetPattern + "$";
    }

    private String functionName(String name) {
        String f = name;
        for (Pattern argPattern : ARG_PATTERNS) {
            Matcher m = argPattern.matcher(f);
            f = m.replaceAll(" ");
        }
        f = f.replaceAll("\\s+", "_");
        return f;
    }

    private String withNamedGroups(String snippetPattern) {
        Matcher m = GROUP_PATTERN.matcher(snippetPattern);

        StringBuffer sb = new StringBuffer();
        int n = 1;
        while (m.find()) {
            m.appendReplacement(sb, "(" + namedGroupStart + n++ + namedGroupEnd);
        }
        m.appendTail(sb);

        return sb.toString();
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
