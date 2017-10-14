package cucumber.java.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FindRegexMatch extends RegexMatch {
    public FindRegexMatch(Pattern regexImpl, String expression) {
        Matcher m = regexImpl.matcher(expression);
        regexMatched = m.matches();
        if (regexMatched) {
            for (int i = 1; i <= m.groupCount(); i++) {
                RegexSubmatch s = new RegexSubmatch();
                s.value = m.group(i);
                s.position = m.start(i);
                submatches.add(s);
            }
        }
    }
}
