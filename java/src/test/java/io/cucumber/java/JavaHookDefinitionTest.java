package io.cucumber.java;

import io.cucumber.core.backend.Lookup;
import io.cucumber.core.backend.TestCaseState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.lang.reflect.Method;
import java.util.List;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings({ "WeakerAccess" })
@ExtendWith({ MockitoExtension.class })
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
public class JavaHookDefinitionTest {

    private final Lookup lookup = new Lookup() {

        @Override
        @SuppressWarnings("unchecked")
        public <T> T getInstance(Class<T> glueClass) {
            return (T) JavaHookDefinitionTest.this;
        }
    };

    @Mock
    private TestCaseState state;

    private boolean invoked = false;

    @Test
    void can_create_with_no_argument() throws Throwable {
        Method method = JavaHookDefinitionTest.class.getMethod("no_arguments");
        JavaHookDefinition definition = new JavaHookDefinition(method, "", 0, lookup);
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
        JavaHookDefinition definition = new JavaHookDefinition(method, "", 0, lookup);
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
            () -> new JavaHookDefinition(method, "", 0, lookup));
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
            () -> new JavaHookDefinition(method, "", 0, lookup));
    }

    public void invalid_generic_parameter(List<String> badType) {

    }

    @Test
    void fails_if_too_many_arguments() throws NoSuchMethodException {
        Method method = JavaHookDefinitionTest.class.getMethod("too_many_parameters", Scenario.class, String.class);
        assertThrows(
            InvalidMethodSignatureException.class,
            () -> new JavaHookDefinition(method, "", 0, lookup));
    }

    public void too_many_parameters(Scenario arg1, String arg2) {

    }

}
