package io.cucumber.guice.matcher;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import java.util.Collection;
import java.util.HashSet;

abstract class ElementsAreAllMatcher<E> extends TypeSafeDiagnosingMatcher<Collection<? extends E>> {

    @Override
    public void describeTo(Description description) {
        description.appendText(getDescription());
    }

    abstract String getDescription();

    boolean containsMoreThanOneElement(Collection<? extends E> item, Description mismatchDescription) {
        return item.size() > 1 || fail("collection did not contain more than one element", item, mismatchDescription);
    }

    protected boolean fail(String reasonForFailure, Collection<? extends E> item, Description mismatchDescription) {
        mismatchDescription.appendText(reasonForFailure);
        mismatchDescription.appendText(" <");
        mismatchDescription.appendText(item.toString());
        mismatchDescription.appendText(">");
        return false;
    }

    boolean noElementIsNull(Collection<? extends E> item, Description mismatchDescription) {
        return !item.contains(null) || fail("collection contained null element", item, mismatchDescription);
    }

    int actualNumberOfUniqueElements(Collection<? extends E> item) {
        return new HashSet<E>(item).size();
    }

}
