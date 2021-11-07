package io.cucumber.java;

import io.cucumber.java.en.Given;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MethodScannerTest {

    private final List<Map.Entry<Method, Annotation>> scanResult = new ArrayList<>();

    private void addScanResult(Method method, Annotation annotation) {
        scanResult.add(new SimpleEntry<>(method, annotation));
    }

    @BeforeEach
    void createBackend() {

    }

    @Test
    void scan_finds_annotated_methods_in_public_class() throws NoSuchMethodException {
        Method publicMethod = BaseSteps.class.getMethod("m");
        Method packagePrivateMethod = BaseSteps.class.getDeclaredMethod("n");
        Method protectedMethod = BaseSteps.class.getDeclaredMethod("o");
        MethodScanner.scan(BaseSteps.class, this::addScanResult);
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
        MethodScanner.scan(ProtectedSteps.class, this::addScanResult);
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
        MethodScanner.scan(PackagePrivateSteps.class, this::addScanResult);
        assertThat(scanResult,
            contains(new SimpleEntry<>(publicMethod, publicMethod.getAnnotations()[0]),
                new SimpleEntry<>(packagePrivateMethod, packagePrivateMethod.getAnnotations()[0]),
                new SimpleEntry<>(protectedMethod, protectedMethod.getAnnotations()[0])));
    }

    @Test
    void scan_ignores_private_class() {
        MethodScanner.scan(PrivateSteps.class, this::addScanResult);
        assertThat(scanResult, empty());
    }

    @Test
    void scan_ignores_object() {
        MethodScanner.scan(Object.class, this::addScanResult);
        assertThat(scanResult, empty());
    }

    @Test
    void scan_ignores_bridge_methods() throws NoSuchMethodException {
        Method method = SpecializedReturnType.class.getMethod("test");
        MethodScanner.scan(SpecializedReturnType.class, this::addScanResult);
        assertThat(scanResult, contains(new SimpleEntry<>(method, method.getAnnotations()[0])));
    }

    @Test
    void scan_ignores_non_instantiable_class() {
        MethodScanner.scan(NonStaticInnerClass.class, this::addScanResult);
        assertThat(scanResult, empty());
    }

    @Test
    void loadGlue_fails_when_class_is_not_method_declaring_class() {
        InvalidMethodException exception = assertThrows(InvalidMethodException.class,
            () -> MethodScanner.scan(ExtendedSteps.class, this::addScanResult));
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
        @SuppressWarnings("unused")
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
        @SuppressWarnings("unused")
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
        @SuppressWarnings("unused")
        private void p() {
        }

    }

    @SuppressWarnings("FinalClass")
    private static class PrivateSteps {

        @Before
        @SuppressWarnings({ "EffectivelyPrivate", "unused" })
        public void m() {
        }

        @Before
        @SuppressWarnings("unused")
        void n() {
        }

        @Before
        @SuppressWarnings({ "EffectivelyPrivate", "unused" })
        protected void o() {
        }

        @Before
        @SuppressWarnings("unused")
        private void p() {
        }

    }

    @SuppressWarnings({ "InnerClassMayBeStatic", "ClassCanBeStatic" })
    public class NonStaticInnerClass {

        @Before
        public void m() {
        }

    }

    public interface GenericReturnType {
        Number test();

    }

    public static class SpecializedReturnType implements GenericReturnType {

        @Given("test")
        @Override
        public Integer test() {
            return 1;
        }

    }
}
