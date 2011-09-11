package cucumber.runtime;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParameterPatternExchanger {

    private final Pattern pattern;

    public ParameterPatternExchanger(Pattern pattern) {
        this.pattern = pattern;
    }

    public String replaceMatches(String name) {
        String replacement = pattern.pattern();
        return replaceMatchWith(name, replacement);
    }

    public String replaceMatchWithSpace(String name) {
        return replaceMatchWith(name, " ");
    }

    private String replaceMatchWith(String name, String replacement) {
        Matcher matcher = pattern.matcher(name);
        return matcher.replaceAll(Matcher.quoteReplacement(replacement));
    }
}