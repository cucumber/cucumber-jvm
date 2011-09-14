package cucumber.runtime;

import org.junit.Test;

import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;

public class ArgumentPatternTest {
    private Class<?> anyType = Integer.TYPE;
    private Pattern singleDigit = Pattern.compile("(\\d)");
    private ArgumentPattern exchanger = new ArgumentPattern(singleDigit, anyType);

    @Test
    public void replacesMatchWithPattern() {
        assertEquals("(\\\\d)", exchanger.replaceMatchesWithGroups("1"));
    }

    @Test
    public void replacesMultipleMatchesWithPattern() {
        assertEquals("(\\\\d)(\\\\d)", exchanger.replaceMatchesWithGroups("13"));
    }

    @Test
    public void replaceMatchWithSpace() throws Exception {
        assertEquals(" ", exchanger.replaceMatchesWithSpace("4"));
    }
}