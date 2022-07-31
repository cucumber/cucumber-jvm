package io.cucumber.guice.matcher;

import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static io.cucumber.guice.matcher.AbstractMatcherTest.assertDescription;
import static io.cucumber.guice.matcher.AbstractMatcherTest.assertDoesNotMatch;
import static io.cucumber.guice.matcher.AbstractMatcherTest.assertMatches;
import static io.cucumber.guice.matcher.AbstractMatcherTest.assertMismatchDescription;
import static io.cucumber.guice.matcher.AbstractMatcherTest.assertNullSafe;
import static io.cucumber.guice.matcher.AbstractMatcherTest.assertUnknownTypeSafe;
import static org.junit.jupiter.api.Assertions.assertAll;

class ElementsAreAllEqualMatcherTest {

    private final Matcher<Collection<?>> matcher = ElementsAreAllEqualMatcher.elementsAreAllEqual();

    @Test
    void testDoesNotMatchNullCollection() {
        Collection<?> arg = null;

        assertAll(
            () -> assertDoesNotMatch(matcher, arg),
            () -> assertMismatchDescription("was null", matcher, arg));
    }

    @Test
    void testDoesNotMatchCollectionWithLessThanTwoElements() {
        Collection<String> arg = Collections.singletonList("foo");

        assertAll(
            () -> assertDoesNotMatch(matcher, arg),
            () -> assertMismatchDescription("collection did not contain more than one element <[foo]>", matcher, arg));
    }

    @Test
    void testDoesNotMatchCollectionWithNullElements() {
        Collection<Object> arg = Arrays.asList(null, null);

        assertAll(
            () -> assertDoesNotMatch(matcher, arg),
            () -> assertMismatchDescription("collection contained null element <[null, null]>", matcher, arg));
    }

    @Test
    void testMatchesCollectionWithTwoElementsThatAreEqual() {
        assertMatches(matcher, Arrays.asList("foo", "foo"));
    }

    @Test
    void testDoesNotMatchCollectionWithTwoElementsThatAreNotEqual() {
        Collection<String> arg = Arrays.asList("foo", "bar");

        assertAll(
            () -> assertDoesNotMatch(matcher, arg),
            () -> assertMismatchDescription("collection contained elements that are not equal <[foo, bar]>", matcher,
                arg));
    }

    @Test
    void testMatchesCollectionWithThreeElementsThatAreEqual() {
        assertMatches(matcher, Arrays.asList("foo", "foo", "foo"));
    }

    @Test
    void testDoesNotMatchCollectionWithSomeElementsThatAreNotEqual() {
        Collection<String> arg = Arrays.asList("foo", "foo", "bar");

        assertAll(
            () -> assertDoesNotMatch(matcher, arg),
            () -> assertMismatchDescription("collection contained elements that are not equal <[foo, foo, bar]>",
                matcher, arg));
    }

    @Test
    void testDoesNotMatchCollectionWithThreeElementsThatAreNotEqual() {
        Collection<String> arg = Arrays.asList("foo", "bar", "baz");

        assertAll(
            () -> assertDoesNotMatch(matcher, arg),
            () -> assertMismatchDescription("collection contained elements that are not equal <[foo, bar, baz]>",
                matcher, arg));
    }

    @Test
    void testMatcherDescription() {
        assertDescription(ElementsAreAllEqualMatcher.DESCRIPTION, matcher);
    }

    @Test
    void testIsNullSafe() {
        assertNullSafe(matcher);
    }

    @Test
    void testCopesWithUnknownTypes() {
        assertUnknownTypeSafe(matcher);
    }

}
