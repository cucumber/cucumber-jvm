package io.cucumber.java;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MethodScannerTest {

    private final List<Map.Entry<Method, Annotation>> scanResult = new ArrayList<>();
    private final BiConsumer<Method, Annotation> backend = (method, annotation) -> scanResult
            .add(new SimpleEntry<>(method, annotation));

    @BeforeEach
    void createBackend() {

    }

    @Test
    void scan_finds_annotated_methods_in_public_class() throws NoSuchMethodException {
        Method publicMethod = BaseSteps.class.getMethod("m");
        Method packagePrivateMethod = BaseSteps.class.getDeclaredMethod("n");
        Method protectedMethod = BaseSteps.class.getDeclaredMethod("o");
        MethodScanner.scan(BaseSteps.class, backend);
        assertThat(scanResult,
            contains(new SimpleEntry<>(publicMethod, publicMethod.getAnnotations()[0]),
                new SimpleEntry<>(packagePrivateMethod, packagePrivateMethod.getAnnotations()[0]),
                new SimpleEntry<>(protectedMethod, protectedMethod.getAnnotations()[0])));
    }

    @Test
    void scan_finds_annotated_methods_in_protected_class() throws NoSuchMethodException {
        Method publicMethod = ProtectedSteps.class.getMethod("m");
        Method packagePrivateMethod = ProtectedSteps.class.getDeclaredMethod("n");
        Method protectedMethod = ProtectedSteps.class.getDeclaredMethod("o");
        MethodScanner.scan(ProtectedSteps.class, backend);
        assertThat(scanResult,
            contains(new SimpleEntry<>(publicMethod, publicMethod.getAnnotations()[0]),
                new SimpleEntry<>(packagePrivateMethod, packagePrivateMethod.getAnnotations()[0]),
                new SimpleEntry<>(protectedMethod, protectedMethod.getAnnotations()[0])));
    }

    @Test
    void scan_finds_annotated_methods_in_package_private_class() throws NoSuchMethodException {
        Method publicMethod = PackagePrivateSteps.class.getMethod("m");
        Method packagePrivateMethod = PackagePrivateSteps.class.getDeclaredMethod("n");
        Method protectedMethod = PackagePrivateSteps.class.getDeclaredMethod("o");
        MethodScanner.scan(PackagePrivateSteps.class, backend);
        assertThat(scanResult,
            contains(new SimpleEntry<>(publicMethod, publicMethod.getAnnotations()[0]),
                new SimpleEntry<>(packagePrivateMethod, packagePrivateMethod.getAnnotations()[0]),
                new SimpleEntry<>(protectedMethod, protectedMethod.getAnnotations()[0])));
    }

    @Test
    void scan_ignores_private_class() {
        MethodScanner.scan(PrivateSteps.class, backend);
        assertThat(scanResult, empty());
    }

    @Test
    void scan_ignores_object() {
        MethodScanner.scan(Object.class, backend);
        assertThat(scanResult, empty());
    }

    @Test
    void scan_ignores_non_instantiable_class() {
        MethodScanner.scan(NonStaticInnerClass.class, backend);
        assertThat(scanResult, empty());
    }

    @Test
    void loadGlue_fails_when_class_is_not_method_declaring_class() {
        InvalidMethodException exception = assertThrows(InvalidMethodException.class,
            () -> MethodScanner.scan(ExtendedSteps.class, backend));
        assertThat(exception.getMessage(), is(
            "You're not allowed to extend classes that define Step Definitions or hooks. " +
                    "class io.cucumber.java.MethodScannerTest$ExtendedSteps extends class io.cucumber.java.MethodScannerTest$BaseSteps"));
    }

    public static class ExtendedSteps extends BaseSteps {

        public interface Interface1 {

        }

    }

    public static class BaseSteps {

        @Before
        public void m() {
        }

        @Before
        void n() {
        }

        @Before
        protected void o() {
        }

        @Before
        private void p() {
        }

    }

    protected static class ProtectedSteps {

        @Before
        public void m() {
        }

        @Before
        void n() {
        }

        @Before
        protected void o() {
        }

        @Before
        private void p() {
        }

    }

    static class PackagePrivateSteps {

        @Before
        public void m() {
        }

        @Before
        void n() {
        }

        @Before
        protected void o() {
        }

        @Before
        private void p() {
        }

    }

    private static class PrivateSteps {

        @Before
        public void m() {
        }

        @Before
        void n() {
        }

        @Before
        protected void o() {
        }

        @Before
        private void p() {
        }

    }

    @SuppressWarnings("InnerClassMayBeStatic")
    public class NonStaticInnerClass {

        @Before
        public void m() {
        }

    }

}
