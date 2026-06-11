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

import static org.assertj.core.api.Assertions.assertThat;
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
        Method publicMethod = PublicClassStepDefinitions.class.getMethod("m");
        Method packagePrivateMethod = PublicClassStepDefinitions.class.getDeclaredMethod("n");
        Method protectedMethod = PublicClassStepDefinitions.class.getDeclaredMethod("o");
        MethodScanner.scan(PublicClassStepDefinitions.class, this::addScanResult);
        assertThat(scanResult)
                .containsExactlyInAnyOrder(
                    new SimpleEntry<>(publicMethod, publicMethod.getAnnotations()[0]),
                    new SimpleEntry<>(packagePrivateMethod, packagePrivateMethod.getAnnotations()[0]),
                    new SimpleEntry<>(protectedMethod, protectedMethod.getAnnotations()[0]));
    }

    @Test
    void scan_finds_annotated_methods_in_protected_class() throws NoSuchMethodException {
        Method publicMethod = ProtectedClassStepDefinitions.class.getMethod("m");
        Method packagePrivateMethod = ProtectedClassStepDefinitions.class.getDeclaredMethod("n");
        Method protectedMethod = ProtectedClassStepDefinitions.class.getDeclaredMethod("o");
        MethodScanner.scan(ProtectedClassStepDefinitions.class, this::addScanResult);
        assertThat(scanResult)
                .containsExactlyInAnyOrder(
                    new SimpleEntry<>(publicMethod, publicMethod.getAnnotations()[0]),
                    new SimpleEntry<>(packagePrivateMethod, packagePrivateMethod.getAnnotations()[0]),
                    new SimpleEntry<>(protectedMethod, protectedMethod.getAnnotations()[0]));
    }

    @Test
    void scan_finds_annotated_methods_in_package_private_class() throws NoSuchMethodException {
        Method publicMethod = PackagePrivateClassStepDefinitions.class.getMethod("m");
        Method packagePrivateMethod = PackagePrivateClassStepDefinitions.class.getDeclaredMethod("n");
        Method protectedMethod = PackagePrivateClassStepDefinitions.class.getDeclaredMethod("o");
        MethodScanner.scan(PackagePrivateClassStepDefinitions.class, this::addScanResult);
        assertThat(scanResult)
                .containsExactlyInAnyOrder(
                    new SimpleEntry<>(publicMethod, publicMethod.getAnnotations()[0]),
                    new SimpleEntry<>(packagePrivateMethod, packagePrivateMethod.getAnnotations()[0]),
                    new SimpleEntry<>(protectedMethod, protectedMethod.getAnnotations()[0]));
    }

    @Test
    void scan_ignores_private_class() {
        MethodScanner.scan(PrivateClassStepDefinitions.class, this::addScanResult);
        assertThat(scanResult).isEmpty();
    }

    @Test
    void scan_ignores_object() {
        MethodScanner.scan(Object.class, this::addScanResult);
        assertThat(scanResult).isEmpty();
    }

    @Test
    void scan_ignores_bridge_methods() throws NoSuchMethodException {
        Method method = SpecializedReturnType.class.getMethod("test");
        MethodScanner.scan(SpecializedReturnType.class, this::addScanResult);
        assertThat(scanResult).containsExactlyInAnyOrder(new SimpleEntry<>(method, method.getAnnotations()[0]));
    }

    @Test
    void scan_ignores_non_instantiable_class() {
        MethodScanner.scan(NonStaticInnerClass.class, this::addScanResult);
        assertThat(scanResult).isEmpty();
    }

    @Test
    void scan_fails_when_class_is_not_method_declaring_class() {
        InvalidMethodException exception = assertThrows(InvalidMethodException.class,
            () -> MethodScanner.scan(ExtendedSteps.class, this::addScanResult));
        assertThat(exception).hasMessageStartingWith(
            "\"io.cucumber.java.MethodScannerTest$ExtendedSteps\" extends \"io.cucumber.java.MethodScannerTest$PublicClassStepDefinitions\" which declares a step definition or hook \"io.cucumber.java.MethodScannerTest$PublicClassStepDefinitions.n()");
    }

    @Test
    void scan_fails_when_annotated_method_is_private() {
        InvalidMethodException exception = assertThrows(InvalidMethodException.class,
            () -> MethodScanner.scan(PrivateStepDefinitionMethod.class, this::addScanResult));
        assertThat(exception).hasMessageStartingWith(
            "\"io.cucumber.java.MethodScannerTest$PrivateStepDefinitionMethod.p()\" is not valid step definition.");
    }

    @Test
    void scan_fails_when_annotated_method_is_implementing_abstract_class() {
        InvalidMethodException exception = assertThrows(InvalidMethodException.class,
            () -> MethodScanner.scan(ConcreteStepDefinitionMethod.class, this::addScanResult));
        assertThat(exception).hasMessageStartingWith(
            "\"io.cucumber.java.MethodScannerTest$ConcreteStepDefinitionMethod\" extends \"io.cucumber.java.MethodScannerTest$AbstractStepDefinitionMethod\" which declares a step definition or hook \"void io.cucumber.java.MethodScannerTest$AbstractStepDefinitionMethod.p()");
    }

    @Test
    void scan_ignores_step_definitions_in_interfaces() {
        MethodScanner.scan(ImplementedStepDefinitionMethod.class, this::addScanResult);
        assertThat(scanResult).isEmpty();
    }

    public static class ExtendedSteps extends PublicClassStepDefinitions {

        public interface Interface1 {

        }

    }

    public static class PublicClassStepDefinitions {

        @Before
        public void m() {
        }

        @Before
        void n() {
        }

        @Before
        protected void o() {
        }

    }

    protected static class ProtectedClassStepDefinitions {

        @Before
        public void m() {
        }

        @Before
        void n() {
        }

        @Before
        protected void o() {
        }

    }

    static class PackagePrivateClassStepDefinitions {

        @Before
        public void m() {
        }

        @Before
        void n() {
        }

        @Before
        protected void o() {
        }

    }

    @SuppressWarnings("FinalClass")
    private static class PrivateClassStepDefinitions {

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

    }

    @SuppressWarnings("FinalClass")
    static class PrivateStepDefinitionMethod {

        @Before
        @SuppressWarnings("unused")
        private void p() {
        }

    }

    @SuppressWarnings("FinalClass")
    abstract static class AbstractStepDefinitionMethod {

        @Before
        @SuppressWarnings("unused")
        abstract void p();

    }

    @SuppressWarnings("FinalClass")
    static class ConcreteStepDefinitionMethod extends AbstractStepDefinitionMethod {

        @SuppressWarnings("unused")
        @Override
        void p() {

        }

    }

    @SuppressWarnings("FinalClass")
    interface InterfaceStepDefinitionMethod {

        @Before
        void p();

    }

    @SuppressWarnings("FinalClass")
    static class ImplementedStepDefinitionMethod implements InterfaceStepDefinitionMethod {

        @SuppressWarnings("unused")
        @Override
        public void p() {

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
