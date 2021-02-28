package io.cucumber.core.runtime;

import io.cucumber.core.runtime.ScanningTypeRegistryConfigurerSupplier.Reflections;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReflectionsTest {

    @Test
    void public_non_static_inner_classes_are_not_instantiable() {
        assertFalse(Reflections.isInstantiable(NonStaticInnerClass.class));
    }

    @Test
    void public_static_inner_classes_are_instantiable() {
        assertTrue(Reflections.isInstantiable(StaticInnerClass.class));
    }

    public static class StaticInnerClass {

    }

    @SuppressWarnings("InnerClassMayBeStatic")
    public class NonStaticInnerClass {

    }

}
