package io.cucumber.guice.matcher;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.junit.jupiter.api.Assertions;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

abstract class AbstractMatcherTest {

    static <T> void assertMatches(Matcher<T> matcher, T arg) {
        assertTrue(matcher.matches(arg),
            "Expected match, but mismatched because: '" + mismatchDescription(matcher, arg) + "'");
    }

    private static <T> String mismatchDescription(Matcher<? super T> matcher, T arg) {
        Description description = new StringDescription();
        matcher.describeMismatch(arg, description);
        return description.toString().trim();
    }

    static <T> void assertDoesNotMatch(Matcher<? super T> c, T arg) {
        assertFalse(c.matches(arg), "Unexpected match");
    }

    static void assertDescription(String expected, Matcher<?> matcher) {
        Description description = new StringDescription();
        description.appendDescriptionOf(matcher);
        Assertions.assertEquals(expected, description.toString().trim(), "Expected description");
    }

    static <T> void assertMismatchDescription(String expected, Matcher<? super T> matcher, T arg) {
        assertFalse(matcher.matches(arg), "Precondition: Matcher should not match item.");
        Assertions.assertEquals(expected, mismatchDescription(matcher, arg), "Expected mismatch description");
    }

    static void assertNullSafe(Matcher<?> matcher) {
        try {
            matcher.matches(null);
        } catch (Exception e) {
            fail("Matcher was not null safe");
        }
    }

    static void assertUnknownTypeSafe(Matcher<?> matcher) {
        try {
            matcher.matches(new UnknownType());
        } catch (Exception e) {
            fail("Matcher was not unknown type safe");
        }
    }

    private static class UnknownType {

    }

}
