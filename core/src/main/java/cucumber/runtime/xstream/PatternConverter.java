package cucumber.runtime.xstream;

import cucumber.deps.com.thoughtworks.xstream.converters.SingleValueConverter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Converts Strings of the form /hello/im to a {@link Pattern}, using a syntax
 * similar to regexp engines like Ruby, Perl and JavaScript. Flags:
 *
 * <ul>
 *     <li>d : UNIX_LINES</li>
 *     <li>i : CASE_INSENSITIVE</li>
 *     <li>x : COMMENTS</li>
 *     <li>m : MULTILINE</li>
 *     <li>l : LITERAL</li>
 *     <li>s : DOTALL</li>
 *     <li>u : UNICODE_CASE</li>
 *     <li>c : CANON_EQ</li>
 * </ul>
 */
public class PatternConverter implements SingleValueConverter {
    private static final Pattern PATTERN_PATTERN = Pattern.compile("/(.*)/(.+)");

    public String toString(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object fromString(String pattern) {
        Matcher matcher = PATTERN_PATTERN.matcher(pattern);
        if (matcher.matches()) {
            return Pattern.compile(matcher.group(1), flags(matcher.group(2)));
        } else {
            return Pattern.compile(pattern);
        }
    }

    private int flags(String flags) {
        int result = 0;
        for (char c : flags.toCharArray()) {
            result |= flag(c);
        }
        return result;
    }

    private enum FLAG {
        d(Pattern.UNIX_LINES),
        i(Pattern.CASE_INSENSITIVE),
        x(Pattern.COMMENTS),
        m(Pattern.MULTILINE),
        l(Pattern.LITERAL),
        s(Pattern.DOTALL),
        u(Pattern.UNICODE_CASE),
        c(Pattern.CANON_EQ);

        private final int modifier;

        FLAG(int modifier) {
            this.modifier = modifier;
        }
    }

    private int flag(char c) {
        return FLAG.valueOf(String.valueOf(c)).modifier;
    }

    @Override
    public boolean canConvert(Class type) {
        return type.equals(Pattern.class);
    }
}
