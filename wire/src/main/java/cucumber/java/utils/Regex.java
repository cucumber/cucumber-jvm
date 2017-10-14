package cucumber.java.utils;

import java.util.regex.Pattern;

public class Regex {
    private Pattern regexImpl;

    public Regex(String expr) {
        regexImpl = Pattern.compile(expr);
    }

    public RegexMatch find(String expression) {
        return new FindRegexMatch(regexImpl, expression);
    }

    public RegexMatch findAll(String expression) {
        return new FindAllRegexMatch(regexImpl, expression);
    }

    @Override
    public String toString() {
        return regexImpl.toString();
    }
}
