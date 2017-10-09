package cucumber.runtime.snippets;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ArgumentPattern {

    private final Pattern pattern;
    private final Class<?> type;
    private final String replacement;

    public ArgumentPattern(Pattern pattern, Class<?> type) {
        this(pattern, pattern.pattern(), type);
    }

    public ArgumentPattern(Pattern pattern, String replacement, Class<?> type) {
        this.pattern = pattern;
        this.replacement = replacement;
        this.type = type;
    }

    public Pattern pattern() {
        return pattern;
    }

    public Class<?> type() {
        return type;
    }

    public String replaceMatchesWithGroups(String name) {
        return replaceMatchWith(name, replacement);
    }

    public String replaceMatchesWithSpace(String name) {
        return replaceMatchWith(name, " ");
    }

    private String replaceMatchWith(String name, String replacement) {
        Matcher matcher = pattern.matcher(name);
        String quotedReplacement = Matcher.quoteReplacement(replacement);
        return matcher.replaceAll(quotedReplacement);
    }
}