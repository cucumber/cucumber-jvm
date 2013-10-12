package cucumber.runtime.snippets;

import org.junit.Test;

import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;

public class ArgumentPatternTest {
    private Class<?> intType = Integer.TYPE;
    private Pattern singleDigit = Pattern.compile("(\\d)");
    private ArgumentPattern argumentPattern = new ArgumentPattern(singleDigit, intType);

    @Test
    public void replacesMatchWithoutEscapedNumberClass() {
        assertEquals("(\\d)", argumentPattern.replaceMatchesWithGroups("1"));
    }

    @Test
    public void replacesMultipleMatchesWithPattern() {
        assertEquals("(\\d)(\\d)", argumentPattern.replaceMatchesWithGroups("13"));
    }

    @Test
    public void replaceMatchWithSpace() throws Exception {
        assertEquals(" ", argumentPattern.replaceMatchesWithSpace("4"));
    }
}