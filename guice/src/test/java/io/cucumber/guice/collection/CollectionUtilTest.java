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

class CollectionUtilTest {

    private List<String> list;

    @BeforeEach
    void setUp() {
        list = new ArrayList<>();
    }

    @Test
    void testNullPointerExceptionIsThrownWhenListIsNull() {
        Executable testMethod = () -> CollectionUtil.removeAllExceptFirstElement(null);
        NullPointerException expectedThrown = assertThrows(NullPointerException.class, testMethod);
        assertThat(expectedThrown.getMessage(), is(equalTo("List must not be null.")));
    }

    @Test
    void testIllegalArgumentExceptionIsThrownWhenListIsEmpty() {
        Executable testMethod = () -> CollectionUtil.removeAllExceptFirstElement(list);
        IllegalArgumentException expectedThrown = assertThrows(IllegalArgumentException.class, testMethod);
        assertThat(expectedThrown.getMessage(), is(equalTo("List must contain at least one element.")));
    }

    @Test
    void testListIsNotModifiedWhenItContainsOneItem() {
        list.add("foo");
        CollectionUtil.removeAllExceptFirstElement(list);
        assertThatListContainsOneElement("foo");
    }

    private void assertThatListContainsOneElement(String element) {
        assertAll(
            () -> assertThat(list.size(), equalTo(1)),
            () -> assertThat(list.get(0), equalTo(element)));
    }

    @Test
    void testSecondItemIsRemovedWhenListContainsTwoItems() {
        list.add("foo");
        list.add("bar");
        CollectionUtil.removeAllExceptFirstElement(list);
        assertThatListContainsOneElement("foo");
    }

    @Test
    void testSecondAndThirdItemsAreRemovedWhenListContainsThreeItems() {
        list.add("foo");
        list.add("bar");
        list.add("baz");
        CollectionUtil.removeAllExceptFirstElement(list);
        assertThatListContainsOneElement("foo");
    }

}
