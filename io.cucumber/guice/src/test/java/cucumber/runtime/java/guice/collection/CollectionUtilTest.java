package cucumber.runtime.java.guice.collection;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class CollectionUtilTest {

    private List<String> list;

    @Before
    public void setUp() {
        list = new ArrayList<String>();
    }

    @Test(expected = NullPointerException.class)
    public void testNullPointerExceptionIsThrownWhenListIsNull() {
        CollectionUtil.removeAllExceptFirstElement(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalArgumentExceptionIsThrownWhenListIsEmpty() {
        CollectionUtil.removeAllExceptFirstElement(list);
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
        assertThat(list.size(), equalTo(1));
        assertThat(list.get(0), equalTo(element));
    }
}
