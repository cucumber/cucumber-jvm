package io.cucumber.guice.matcher;

import org.hamcrest.Description;
import org.hamcrest.Matcher;

import java.util.Collection;

public class ElementsAreAllEqualMatcher<E> extends ElementsAreAllMatcher<E> {

    static final String DESCRIPTION = "a collection of two or more non-null elements that are determined to be the " +
            "same according to the java.lang.Object.equals() contract";
    private static final int EXPECTED_NUMBER_OF_UNIQUE_ELEMENTS = 1;

    /**
     * Creates a matcher for {@link java.util.Collection}s that matches when
     * there are two or more non-null elements and every element is the same.
     * Two elements are considered the same if element1.equals(element2) returns
     * true. When collections contain more than two elements, every permutation
     * of two elements must return true.
     * <p/>
     * For example:
     * 
     * <pre>
     * assertThat(Arrays.asList("foo", "foo", "foo"), elementsAreAllEqual())
     * </pre>
     */
    public static <E> Matcher<Collection<? extends E>> elementsAreAllEqual() {
        return new ElementsAreAllEqualMatcher<>();
    }

    @Override
    protected boolean matchesSafely(Collection<? extends E> item, Description mismatchDescription) {
        return containsMoreThanOneElement(item, mismatchDescription) && noElementIsNull(item, mismatchDescription) &&
                allElementsAreEqual(item, mismatchDescription);
    }

    private boolean allElementsAreEqual(Collection<? extends E> item, Description mismatchDescription) {
        return actualNumberOfUniqueElements(item) == EXPECTED_NUMBER_OF_UNIQUE_ELEMENTS ||
                fail("collection contained elements that are not equal", item, mismatchDescription);
    }

    @Override
    String getDescription() {
        return DESCRIPTION;
    }

}
