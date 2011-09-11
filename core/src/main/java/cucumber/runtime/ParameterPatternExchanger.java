package cucumber.runtime;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParameterPatternExchanger {

    private final Pattern pattern;
    private final String replacement;
    private final Class<?> type;
    
    
    public static ParameterPatternExchanger ExchangeMatchsWithPattern(Pattern pattern, Class<?> type) {
        return new ParameterPatternExchanger(pattern, pattern.pattern(), type);
    }
    
    public static ParameterPatternExchanger ExchangeMatchesWithReplacement(Pattern pattern, String replacement, Class<?>type) {
        return new ParameterPatternExchanger(pattern, replacement, type);
    }

    public ParameterPatternExchanger(Pattern pattern, String replacement, Class<?> type) {
        this.pattern = pattern;
        this.replacement = replacement;
        this.type = type;
    }
    
    public Pattern pattern() {
        return pattern;
    }
    
    public Class<?> typeForMethodSignature(){
        return type;
    }

    public String exchangeMatches(String name) {
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