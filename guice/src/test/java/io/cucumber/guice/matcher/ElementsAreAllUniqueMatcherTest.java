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

class ElementsAreAllUniqueMatcherTest {

    private final Matcher<Collection<?>> matcher = ElementsAreAllUniqueMatcher.elementsAreAllUnique();

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
    void testDoesNotMatchCollectionWithNullElement() {
        Collection<String> arg = Arrays.asList("foo", null);

        assertAll(
            () -> assertDoesNotMatch(matcher, arg),
            () -> assertMismatchDescription("collection contained null element <[foo, null]>", matcher, arg));
    }

    @Test
    void testMatchesCollectionWithTwoElementsThatAreUnique() {
        assertMatches(matcher, Arrays.asList("foo", "bar"));
    }

    @Test
    void testDoesNotMatchCollectionWithTwoElementsThatAreNotUnique() {
        Collection<String> arg = Arrays.asList("foo", "foo");

        assertAll(
            () -> assertDoesNotMatch(matcher, arg),
            () -> assertMismatchDescription("collection contained elements that are not unique <[foo, foo]>", matcher,
                arg));
    }

    @Test
    void testMatchesCollectionWithThreeElementsThatAreAllUnique() {
        assertMatches(matcher, Arrays.asList("foo", "bar", "baz"));
    }

    @Test
    void testDoesNotMatchCollectionWithElementsThatAreNotUnique() {
        Collection<String> arg = Arrays.asList("foo", "bar", "foo");

        assertAll(
            () -> assertDoesNotMatch(matcher, arg),
            () -> assertMismatchDescription("collection contained elements that are not unique <[foo, bar, foo]>",
                matcher, arg));
    }

    @Test
    void testDoesNotMatchCollectionWithThreeElementsThatAreNotUnique() {
        Collection<String> arg = Arrays.asList("foo", "foo", "foo");

        assertAll(
            () -> assertDoesNotMatch(matcher, arg),
            () -> assertMismatchDescription("collection contained elements that are not unique <[foo, foo, foo]>",
                matcher, arg));
    }

    @Test
    void testMatcherDescription() {
        assertDescription(ElementsAreAllUniqueMatcher.DESCRIPTION, matcher);
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
