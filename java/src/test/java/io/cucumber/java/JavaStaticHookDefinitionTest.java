package io.cucumber.java;

import io.cucumber.core.backend.Lookup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings({ "WeakerAccess" })
public class JavaStaticHookDefinitionTest {

    private final Lookup lookup = new Lookup() {

        @Override
        @SuppressWarnings("unchecked")
        public <T> T getInstance(Class<T> glueClass) {
            return (T) JavaStaticHookDefinitionTest.this;
        }
    };

    private static boolean invoked;

    @BeforeEach
    void reset() {
        invoked = false;
    }

    @Test
    void can_create_with_no_argument() throws Throwable {
        Method method = JavaStaticHookDefinitionTest.class.getMethod("no_arguments");
        JavaStaticHookDefinition definition = new JavaStaticHookDefinition(method, 0, lookup);
        definition.execute();
        assertTrue(invoked);
    }

    @BeforeAll
    public static void no_arguments() {
        invoked = true;
    }

    @Test
    void fails_with_arguments() throws Throwable {
        Method method = JavaStaticHookDefinitionTest.class.getMethod("single_argument", Scenario.class);
        InvalidMethodSignatureException exception = assertThrows(
                InvalidMethodSignatureException.class,
                () -> new JavaStaticHookDefinition(method, 0, lookup));
        assertThat(exception.getMessage(), startsWith("" +
                "A method annotated with BeforeAll or AfterAll must have one of these signatures:\n" +
                " * public static void before_or_after_all()\n" +
                "at io.cucumber.java.JavaStaticHookDefinitionTest.single_argument(io.cucumber.java.Scenario)\n"));
    }

    @Before
    public void single_argument(Scenario scenario) {
        invoked = true;
    }

    @Test
    void fails_with_non_void_return_type() throws Throwable {
        Method method = JavaStaticHookDefinitionTest.class.getMethod("string_return_type");
        InvalidMethodSignatureException exception = assertThrows(
                InvalidMethodSignatureException.class,
                () -> new JavaStaticHookDefinition(method, 0, lookup));
        assertThat(exception.getMessage(), startsWith("" +
                "A method annotated with BeforeAll or AfterAll must have one of these signatures:\n" +
                " * public static void before_or_after_all()\n" +
                "at io.cucumber.java.JavaStaticHookDefinitionTest.string_return_type()\n"));
    }

    @Before
    public String string_return_type() {
        invoked = true;
        return "";
    }

}
