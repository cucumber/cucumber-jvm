package io.cucumber.core.reflection;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ReflectionsTest {
    @Test
    public void public_non_static_inner_classes_are_not_instantiable() {
        assertFalse(Reflections.isInstantiable(NonStaticInnerClass.class));
    }

    @Test
    public void public_static_inner_classes_are_instantiable() {
        assertTrue(Reflections.isInstantiable(StaticInnerClass.class));
    }

    public class NonStaticInnerClass {
    }

    public static class StaticInnerClass {
    }

}
