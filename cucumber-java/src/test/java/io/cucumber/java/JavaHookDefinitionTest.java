package io.cucumber.java;

import io.cucumber.core.backend.Lookup;
import io.cucumber.core.backend.Status;
import io.cucumber.core.backend.TestCaseState;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.Collection;
import java.util.List;

import static io.cucumber.core.backend.HookDefinition.HookType.BEFORE;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings({ "WeakerAccess" })
public class JavaHookDefinitionTest {

    private final Lookup lookup = new Lookup() {

        @Override
        @SuppressWarnings("unchecked")
        public <T> T getInstance(Class<T> glueClass) {
            return (T) JavaHookDefinitionTest.this;
        }
    };

    private TestCaseState state = new StubTestCaseState();

    private boolean invoked = false;

    @Test
    void can_create_with_no_argument() throws Throwable {
        Method method = JavaHookDefinitionTest.class.getMethod("no_arguments");
        JavaHookDefinition definition = new JavaHookDefinition(BEFORE, method, "", 0, lookup);
        definition.execute(state);
        assertTrue(invoked);
    }

    @Before
    public void no_arguments() {
        invoked = true;
    }

    @Test
    void can_create_with_single_scenario_argument() throws Throwable {
        Method method = JavaHookDefinitionTest.class.getMethod("single_argument", Scenario.class);
        JavaHookDefinition definition = new JavaHookDefinition(BEFORE, method, "", 0, lookup);
        definition.execute(state);
        assertTrue(invoked);
    }

    @Before
    public void single_argument(Scenario scenario) {
        invoked = true;
    }

    @Test
    void fails_if_hook_argument_is_not_scenario_result() throws NoSuchMethodException {
        Method method = JavaHookDefinitionTest.class.getMethod("invalid_parameter", String.class);
        InvalidMethodSignatureException exception = assertThrows(
            InvalidMethodSignatureException.class,
            () -> new JavaHookDefinition(BEFORE, method, "", 0, lookup));
        assertThat(exception.getMessage(), startsWith("" +
                "A method annotated with Before, After, BeforeStep or AfterStep must have one of these signatures:\n" +
                " * public void before_or_after(io.cucumber.java.Scenario scenario)\n" +
                " * public void before_or_after()\n" +
                "at io.cucumber.java.JavaHookDefinitionTest.invalid_parameter(java.lang.String"));
    }

    public void invalid_parameter(String badType) {

    }

    @Test
    void fails_if_generic_hook_argument_is_not_scenario_result() throws NoSuchMethodException {
        Method method = JavaHookDefinitionTest.class.getMethod("invalid_generic_parameter", List.class);
        assertThrows(
            InvalidMethodSignatureException.class,
            () -> new JavaHookDefinition(BEFORE, method, "", 0, lookup));
    }

    public void invalid_generic_parameter(List<String> badType) {

    }

    @Test
    void fails_if_too_many_arguments() throws NoSuchMethodException {
        Method method = JavaHookDefinitionTest.class.getMethod("too_many_parameters", Scenario.class, String.class);
        assertThrows(
            InvalidMethodSignatureException.class,
            () -> new JavaHookDefinition(BEFORE, method, "", 0, lookup));
    }

    public void too_many_parameters(Scenario arg1, String arg2) {

    }

    @Test
    void fails_with_non_void_return_type() throws Throwable {
        Method method = JavaHookDefinitionTest.class.getMethod("string_return_type");
        InvalidMethodSignatureException exception = assertThrows(
            InvalidMethodSignatureException.class,
            () -> new JavaHookDefinition(BEFORE, method, "", 0, lookup));
        assertThat(exception.getMessage(), startsWith("" +
                "A method annotated with Before, After, BeforeStep or AfterStep must have one of these signatures:\n" +
                " * public void before_or_after(io.cucumber.java.Scenario scenario)\n" +
                " * public void before_or_after()\n" +
                "at io.cucumber.java.JavaHookDefinitionTest.string_return_type()\n"));
    }

    @Before
    public String string_return_type() {
        invoked = true;
        return "";
    }

    private class StubTestCaseState implements TestCaseState {
        @Override
        public Collection<String> getSourceTagNames() {
            return null;
        }

        @Override
        public Status getStatus() {
            return null;
        }

        @Override
        public boolean isFailed() {
            return false;
        }

        @Override
        public void attach(byte[] data, String mediaType, String name) {

        }

        @Override
        public void attach(String data, String mediaType, String name) {

        }

        @Override
        public void log(String text) {

        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public String getId() {
            return null;
        }

        @Override
        public URI getUri() {
            return null;
        }

        @Override
        public Integer getLine() {
            return null;
        }
    }
}
