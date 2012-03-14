package cucumber.runtime;

import org.junit.Test;

import static cucumber.runtime.Utils.*;
import static org.junit.Assert.*;

public class UtilsTest {
    @Test
    public void finds_path_for_class_in_package() {
        assertEquals("java.lang", packageName("java.lang.String"));
        assertEquals("java/lang", packagePath(String.class));
    }

    @Test
    public void finds_path_for_class_in_toplevel_package() {
        assertEquals("", packageName("TopLevelClass"));
        assertEquals("", packagePath(packageName("TopLevelClass")));
    }

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
}
