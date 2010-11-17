package cuke4duke.internal.jvmclass;

import cucumber.runtime.java.ObjectFactory;
import cucumber.runtime.java.pico.PicoFactory;
import cuke4duke.StepMother;
import cuke4duke.annotation.Order;
import cuke4duke.internal.java.JavaAnalyzer;
import cuke4duke.internal.java.JavaHook;
import cuke4duke.spi.ExceptionFactory;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InOrder;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

public class ClassLanguageTest {
    private ClassLanguage language;

    @Before
    public void createLanguage() throws Throwable {
        language = null; //new ClassLanguage(mock(ClassLanguageMixin.class), mock(ExceptionFactory.class), mock(StepMother.class), Collections.<ClassAnalyzer> emptyList());
    }

    @Test
    public void dummy() throws Throwable {
    }

    @Test
    @Ignore
    public void shouldLoadExistingClassFromJavaFileName() throws Throwable {
        language.load_code_file("foo/java/lang/String.class");
    }

    @Test(expected = ClassNotFoundException.class)
    @Ignore
    public void shouldFailToLoadMissingClassFromJavaFileName() throws Throwable {
        language.load_code_file("foo/java/lang/Strix.class");
    }

    public static class A {
        private final B b;

        public A(B b) {
            this.b = b;
        }

        @cuke4duke.annotation.Before()
        @Order(1)
        public void doA() {
            assertEquals("Heldlo from B", b.message);
        }
    }

    public static class B {
        public String message;

        @cuke4duke.annotation.Before()
        @Order(2)
        public void doB() {
            message = "Hello from B";
        }
    }

    @Test
    @Ignore
    public void shouldRunBeforeHooksInOrderOfDependencies() throws Throwable {
        ObjectFactory objectFactory = new PicoFactory();
        ClassLanguageMixin languageMixin = mock(ClassLanguageMixin.class);
        language = new ClassLanguage(languageMixin, mock(ExceptionFactory.class), mock(StepMother.class), Collections.<ClassAnalyzer>singletonList(new JavaAnalyzer()), objectFactory);
        language.addClass(A.class);
        language.addClass(B.class);

        language.begin_scenario(null);

        InOrder order = inOrder(languageMixin);
        order.verify(languageMixin).add_hook(eq("before"), argThat(isHook("doA")));
        order.verify(languageMixin).add_hook(eq("before"), argThat(isHook("doB")));
    }

    private Matcher<JavaHook> isHook(String methodName) {
        return new HookMatcher(methodName);
    }

    private class HookMatcher extends BaseMatcher<JavaHook> {
        private final String methodName;
        private String actualMethodName;

        public HookMatcher(String methodName) {
            this.methodName = methodName;
        }

        public void describeTo(Description description) {
            description.appendText("Expected " + methodName + ", but got " + actualMethodName);
        }

        public boolean matches(Object o) {
            JavaHook hook = (JavaHook) o;
            actualMethodName = hook.getMethod().getName();
            return methodName.equals(actualMethodName);
        }
    }

}
