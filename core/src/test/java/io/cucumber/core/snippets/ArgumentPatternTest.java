package io.cucumber.core.snippets;

import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

public class ArgumentPatternTest {

    private Pattern singleDigit = Pattern.compile("(\\d)");
    private ArgumentPattern argumentPattern = new ArgumentPattern(singleDigit);

    @Test
    public void replacesMatchWithoutEscapedNumberClass() {
        assertThat(argumentPattern.replaceMatchesWithGroups("1"), is(equalTo("(\\d)")));
    }

    @Test
    public void replacesMultipleMatchesWithPattern() {
        assertThat(argumentPattern.replaceMatchesWithGroups("13"), is(equalTo("(\\d)(\\d)")));
    }

    @Test
    public void replaceMatchWithSpace() {
        assertThat(argumentPattern.replaceMatchesWithSpace("4"), is(equalTo(" ")));
    }

}
