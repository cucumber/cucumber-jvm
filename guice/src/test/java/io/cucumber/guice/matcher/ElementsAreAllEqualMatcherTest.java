package io.cucumber.guice.matcher;

import org.hamcrest.Matcher;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertAll;

public class ElementsAreAllEqualMatcherTest extends AbstractMatcherTest {

    private final Matcher<Collection<?>> matcher = ElementsAreAllEqualMatcher.elementsAreAllEqual();

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

    public void testDoesNotMatchCollectionWithNullElements() {
        Collection<Object> arg = Arrays.asList(null, null);

        assertAll("Checking Matcher",
            () -> assertDoesNotMatch(matcher, arg),
            () -> assertMismatchDescription("collection contained null element <[null, null]>", matcher, arg)
        );
    }

    public void testMatchesCollectionWithTwoElementsThatAreEqual() {
        assertMatches(matcher, Arrays.asList("foo", "foo"));
    }

    public void testDoesNotMatchCollectionWithTwoElementsThatAreNotEqual() {
        Collection<String> arg = Arrays.asList("foo", "bar");

        assertAll("Checking Matcher",
            () -> assertDoesNotMatch(matcher, arg),
            () -> assertMismatchDescription("collection contained elements that are not equal <[foo, bar]>", matcher, arg)
        );
    }

    public void testMatchesCollectionWithThreeElementsThatAreEqual() {
        assertMatches(matcher, Arrays.asList("foo", "foo", "foo"));
    }

    public void testDoesNotMatchCollectionWithSomeElementsThatAreNotEqual() {
        Collection<String> arg = Arrays.asList("foo", "foo", "bar");

        assertAll("Checking Matcher",
            () -> assertDoesNotMatch(matcher, arg),
            () -> assertMismatchDescription("collection contained elements that are not equal <[foo, foo, bar]>", matcher, arg)
        );
    }

    public void testDoesNotMatchCollectionWithThreeElementsThatAreNotEqual() {
        Collection<String> arg = Arrays.asList("foo", "bar", "baz");

        assertAll("Checking Matcher",
            () -> assertDoesNotMatch(matcher, arg),
            () -> assertMismatchDescription("collection contained elements that are not equal <[foo, bar, baz]>", matcher, arg)
        );
    }

    public void testMatcherDescription() {
        assertDescription(ElementsAreAllEqualMatcher.DESCRIPTION, matcher);
    }
}
