package cucumber.runtime;

import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;

import static cucumber.runtime.Utils.isInstantiable;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UtilsTest {
    @Test
    public void public_non_static_inner_classes_are_not_instantiable() {
        assertFalse(isInstantiable(NonStaticInnerClass.class));
    }

    @Test
    public void public_static_inner_classes_are_instantiable() {
        assertTrue(isInstantiable(StaticInnerClass.class));
    }

    public class NonStaticInnerClass {
    }

    public static class StaticInnerClass {
    }

    @Test
    public void test_url() throws MalformedURLException {
        URL dotCucumber = Utils.toURL("foo/bar/.cucumber");
        URL url = new URL(dotCucumber, "stepdefs.json");
        assertEquals(new URL("file:foo/bar/.cucumber/stepdefs.json"), url);
    }
}
