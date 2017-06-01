package cucumber.runtime.java.hk2.matcher;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;

import java.util.Collection;

public class ElementsAreAllEqualMatcher<E> extends ElementsAreAllMatcher<E> {

    private static final int EXPECTED_NUMBER_OF_UNIQUE_ELEMENTS = 1;
    static final String DESCRIPTION =
            "a collection of two or more non-null elements that are determined to be the same according to " +
                    "the java.lang.Object.equals() contract";

    /**
     * Creates a matcher for {@link Collection}s that matches when there are two or more non-null elements and
     * every element is the same. Two elements are considered the same if element1.equals(element2) returns true. When
     * collections contain more than two elements, every permutation of two elements must return true.
     * <p/>
     * For example:
     * <pre>assertThat(Arrays.asList("foo", "foo", "foo"), elementsAreAllEqual())</pre>
     */
    /**
     * Creates a matcher for {@link Collection}s that matches when every element is the same. Elements are
     * tested for equality using java.lang.Object.equals().
     * <p/>
     * For example:
     * <pre>assertThat(Arrays.asList("foo", "foo", "foo"), elementsAreAllEqual())</pre>
     */
    @Factory
    public static <E> Matcher<Collection<? extends E>> elementsAreAllEqual() {
        return new ElementsAreAllEqualMatcher<E>();
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
