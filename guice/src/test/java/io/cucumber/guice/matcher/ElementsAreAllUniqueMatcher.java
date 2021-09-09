package io.cucumber.guice.matcher;

import org.hamcrest.Description;
import org.hamcrest.Matcher;

import java.util.Collection;

public class ElementsAreAllUniqueMatcher<E> extends ElementsAreAllMatcher<E> {

    static final String DESCRIPTION = "a collection of two or more non-null elements that are determined to be unique" +
            " according to the java.lang.Object.equals() contract";

    /**
     * Creates a matcher for {@link java.util.Collection}s that matches when
     * there are two or more non-null elements and every element is unique. Two
     * elements are considered unique if element1.equals(element2) returns
     * false. When collections contain more than two elements, every permutation
     * of two elements must return false.
     * <p/>
     * For example:
     * 
     * <pre>
     * assertThat(Arrays.asList("foo", "bar", "baz"), elementsAreAllUnique())
     * </pre>
     */
    public static <E> Matcher<Collection<? extends E>> elementsAreAllUnique() {
        return new ElementsAreAllUniqueMatcher<>();
    }

    @Override
    protected boolean matchesSafely(Collection<? extends E> item, Description mismatchDescription) {
        return containsMoreThanOneElement(item, mismatchDescription) && noElementIsNull(item, mismatchDescription) &&
                allElementsAreUnique(item, mismatchDescription);
    }

    private boolean allElementsAreUnique(Collection<? extends E> item, Description mismatchDescription) {
        return actualNumberOfUniqueElements(item) == expectedNumberOfUniqueElements(item)
                || fail("collection contained elements that are not unique", item, mismatchDescription);
    }

    private int expectedNumberOfUniqueElements(Collection<? extends E> item) {
        return item.size();
    }

    @Override
    String getDescription() {
        return DESCRIPTION;
    }

}
