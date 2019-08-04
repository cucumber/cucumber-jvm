package io.cucumber.guice.collection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CollectionUtilTest {

    private List<String> list;

    @BeforeEach
    public void setUp() {
        list = new ArrayList<String>();
    }

    @Test
    public void testNullPointerExceptionIsThrownWhenListIsNull() {
        final Executable testMethod = () -> CollectionUtil.removeAllExceptFirstElement(null);
        final NullPointerException expectedThrown = assertThrows(NullPointerException.class, testMethod);
        assertThat(expectedThrown.getMessage(), is(equalTo("List must not be null.")));
    }

    @Test
    public void testIllegalArgumentExceptionIsThrownWhenListIsEmpty() {
        final Executable testMethod = () -> CollectionUtil.removeAllExceptFirstElement(list);
        final IllegalArgumentException expectedThrown = assertThrows(IllegalArgumentException.class, testMethod);
        assertThat(expectedThrown.getMessage(), is(equalTo("List must contain at least one element.")));
    }

    @Test
    public void testListIsNotModifiedWhenItContainsOneItem() {
        list.add("foo");
        CollectionUtil.removeAllExceptFirstElement(list);
        assertThatListContainsOneElement("foo");
    }

    @Test
    public void testSecondItemIsRemovedWhenListContainsTwoItems() {
        list.add("foo");
        list.add("bar");
        CollectionUtil.removeAllExceptFirstElement(list);
        assertThatListContainsOneElement("foo");
    }

    @Test
    public void testSecondAndThirdItemsAreRemovedWhenListContainsThreeItems() {
        list.add("foo");
        list.add("bar");
        list.add("baz");
        CollectionUtil.removeAllExceptFirstElement(list);
        assertThatListContainsOneElement("foo");
    }

    private void assertThatListContainsOneElement(String element) {
        assertAll("Checking list",
            () -> assertThat(list.size(), equalTo(1)),
            () -> assertThat(list.get(0), equalTo(element))
        );
    }

}
