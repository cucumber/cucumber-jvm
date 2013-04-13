package cucumber.runtime;

import gherkin.formatter.Argument;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class JdkPatternArgumentMatcherTest {
    @Test
    public void shouldDealWithOnlyAscii() throws UnsupportedEncodingException {
        assertVariables("Ja (.+) elsker (.+) landet", "Ja vi elsker dette landet", "vi", 3, "dette", 13);
    }

    @Test
    public void shouldDealWithUnicodeInsideCaptures() throws UnsupportedEncodingException {
        assertVariables("Ja (.+) elsker (.+) landet", "Ja vø elsker døtte landet", "vø", 3, "døtte", 13);
    }

    @Test
    public void shouldDealWithUnicodeOutsideCaptures() throws UnsupportedEncodingException {
        assertVariables("Jæ (.+) ålsker (.+) lændet", "Jæ vi ålsker dette lændet", "vi", 3, "dette", 13);
    }

    @Test
    public void shouldDealWithUnicodeEverywhere() throws UnsupportedEncodingException {
        assertVariables("Jæ (.+) ålsker (.+) lændet", "Jæ vø ålsker døtte lændet", "vø", 3, "døtte", 13);
    }

    @Test
    public void shouldDealWithUnAnchoredPattern() throws UnsupportedEncodingException {
        assertVariables("^I wait for (.+) and (.+) seconds",
                "I wait for 3 and 4 seconds to be sure",
                "3", 11, "4", 17
        );
    }

    @Test
    public void shouldDealWithAnchoredPattern() {
        JdkPatternArgumentMatcher matcher = new JdkPatternArgumentMatcher(Pattern.compile("^I wait for (.+) seconds$"));

        assertNull(matcher.argumentsFrom("I wait for 30 seconds to be sure"));
        assertEquals(1, matcher.argumentsFrom("I wait for 30 seconds").size());
    }

    private void assertVariables(String regex, String string, String v1, Integer pos1, String v2, Integer pos2) throws UnsupportedEncodingException {
        List<Argument> args = new JdkPatternArgumentMatcher(Pattern.compile(regex)).argumentsFrom(string);
        assertEquals(2, args.size());
        assertEquals(v1, args.get(0).getVal());
        assertEquals(pos1, args.get(0).getOffset());
        assertEquals(v2, args.get(1).getVal());
        assertEquals(pos2, args.get(1).getOffset());
    }
}
