package cucumber.runtime;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParameterPatternExchanger {

    private final Pattern pattern;
    private final Class<?> type;

    public ParameterPatternExchanger(Pattern pattern, Class<?> type ) {
        this.pattern = pattern;
        this.type = type;
    }
    
    public Pattern pattern() {
        return pattern;
    }
    
    public Class<?> typeForMethodSignature(){
        return type;
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