package io.cucumber.guice.matcher;

import org.hamcrest.Matcher;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertAll;

public class ElementsAreAllUniqueMatcherTest extends AbstractMatcherTest {

    private final Matcher<Collection<?>> matcher = ElementsAreAllUniqueMatcher.elementsAreAllUnique();

    @Override
    protected Matcher<?> createMatcher() {
        return matcher;
    }

    public void testDoesNotMatchNullCollection() {
        Collection<?> arg = null;

        assertAll("Checking Matcher",
            () -> assertDoesNotMatch(matcher, arg),
            () -> assertMismatchDescription("was null", matcher, arg)
        );
    }

    public void testDoesNotMatchCollectionWithLessThanTwoElements() {
        Collection<String> arg = Arrays.asList("foo");

        assertAll("Checking Matcher",
            () -> assertDoesNotMatch(matcher, arg),
            () -> assertMismatchDescription("collection did not contain more than one element <[foo]>", matcher, arg)
        );
    }

    public void testDoesNotMatchCollectionWithNullElement() {
        Collection<String> arg = Arrays.asList("foo", null);

        assertAll("Checking Matcher",
            () -> assertDoesNotMatch(matcher, arg),
            () -> assertMismatchDescription("collection contained null element <[foo, null]>", matcher, arg)
        );
    }

    public void testMatchesCollectionWithTwoElementsThatAreUnique() {
        assertMatches(matcher, Arrays.asList("foo", "bar"));
    }

    public void testDoesNotMatchCollectionWithTwoElementsThatAreNotUnique() {
        Collection<String> arg = Arrays.asList("foo", "foo");

        assertAll("Checking Matcher",
            () -> assertDoesNotMatch(matcher, arg),
            () -> assertMismatchDescription("collection contained elements that are not unique <[foo, foo]>", matcher, arg)
        );
    }

    public void testMatchesCollectionWithThreeElementsThatAreAllUnique() {
        assertMatches(matcher, Arrays.asList("foo", "bar", "baz"));
    }

    public void testDoesNotMatchCollectionWithElementsThatAreNotUnique() {
        Collection<String> arg = Arrays.asList("foo", "bar", "foo");

        assertAll("Checking Matcher",
            () -> assertDoesNotMatch(matcher, arg),
            () -> assertMismatchDescription("collection contained elements that are not unique <[foo, bar, foo]>", matcher, arg)
        );
    }

    public void testDoesNotMatchCollectionWithThreeElementsThatAreNotUnique() {
        Collection<String> arg = Arrays.asList("foo", "foo", "foo");

        assertAll("Checking Matcher",
            () -> assertDoesNotMatch(matcher, arg),
            () -> assertMismatchDescription("collection contained elements that are not unique <[foo, foo, foo]>", matcher, arg)
        );
    }

    public void testMatcherDescription() {
        assertDescription(ElementsAreAllUniqueMatcher.DESCRIPTION, matcher);
    }
}
