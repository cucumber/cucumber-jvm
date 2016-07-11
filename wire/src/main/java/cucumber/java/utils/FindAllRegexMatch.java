package cucumber.java.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FindAllRegexMatch extends RegexMatch {
    public FindAllRegexMatch(Pattern regexImpl, String expression) {
        regexMatched = false;
        Matcher m = regexImpl.matcher(expression);
        while (m.find()) {
            regexMatched = true;
            RegexSubmatch s = new RegexSubmatch();
            s.value = m.group();
            s.position = -1;
            submatches.add(s);
        }
    }
}
