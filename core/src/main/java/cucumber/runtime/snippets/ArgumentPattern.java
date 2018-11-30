package cucumber.runtime.snippets;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ArgumentPattern {

    private final Pattern pattern;
    private final String replacement;

    ArgumentPattern(Pattern pattern) {
        this(pattern, pattern.pattern());
    }

    private ArgumentPattern(Pattern pattern, String replacement) {
        this.pattern = pattern;
        this.replacement = replacement;
    }

    String replaceMatchesWithGroups(String name) {
        return replaceMatchWith(name, replacement);
    }

    String replaceMatchesWithSpace(String name) {
        return replaceMatchWith(name, " ");
    }

    private String replaceMatchWith(String name, String replacement) {
        Matcher matcher = pattern.matcher(name);
        String quotedReplacement = Matcher.quoteReplacement(replacement);
        return matcher.replaceAll(quotedReplacement);
    }
}