package io.cucumber.core.plugin;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.util.Objects;

public final class IsEqualCompressingLineSeparators extends TypeSafeMatcher<String> {

    private final String expected;

    public IsEqualCompressingLineSeparators(String expected) {
        Objects.requireNonNull(expected);
        this.expected = expected;
    }

    public String getExpected() {
        return expected;
    }

    @Override
    public boolean matchesSafely(String actual) {
        return compressNewLines(expected).equals(compressNewLines(actual));
    }

    @Override
    public void describeMismatchSafely(String item, Description mismatchDescription) {
        mismatchDescription.appendText("was ").appendValue(item);
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("a string equal to ")
                .appendValue(expected)
                .appendText(" compressing newlines");
    }

    public String compressNewLines(String actual) {
        return actual.replaceAll("[\r\n]+", "\n").trim();
    }

    public static Matcher<String> equalCompressingLineSeparators(String expectedString) {
        return new IsEqualCompressingLineSeparators(expectedString);
    }

}
