package io.cucumber.java;

import io.cucumber.core.backend.Lookup;
import io.cucumber.core.backend.TestCaseState;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.lang.reflect.Method;
import java.util.List;

import static io.cucumber.core.backend.HookDefinition.HookType.AFTER;
import static io.cucumber.core.backend.HookDefinition.HookType.AFTER_STEP;
import static io.cucumber.core.backend.HookDefinition.HookType.BEFORE;
import static io.cucumber.core.backend.HookDefinition.HookType.BEFORE_STEP;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("WeakerAccess")
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class JavaHookDefinitionTest {

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
        assertThat(exception.getMessage(), startsWith("""
                A @Before annotated method must have one of these signatures:
                 * public void hook()
                 * public void hook(io.cucumber.java.Scenario scenario)
                at io.cucumber.java.JavaHookDefinitionTest.invalid_parameter(java.lang.String"""));
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
        assertThat(exception.getMessage(), startsWith("""
                A @Before annotated method must have one of these signatures:
                 * public void hook()
                 * public void hook(io.cucumber.java.Scenario scenario)
                at io.cucumber.java.JavaHookDefinitionTest.string_return_type()
                """));
    }

    @Before
    public String string_return_type() {
        invoked = true;
        return "";
    }

    // Step hook tests

    @Test
    void can_create_step_hook_with_no_argument() throws Throwable {
        Method method = JavaHookDefinitionTest.class.getMethod("step_hook_no_arguments");
        JavaHookDefinition definition = new JavaHookDefinition(BEFORE_STEP, method, "", 0, lookup);
        definition.execute(state);
        assertTrue(invoked);
    }

    @BeforeStep
    public void step_hook_no_arguments() {
        invoked = true;
    }

    @Test
    void can_create_step_hook_with_single_scenario_argument() throws Throwable {
        Method method = JavaHookDefinitionTest.class.getMethod("step_hook_single_argument", Scenario.class);
        JavaHookDefinition definition = new JavaHookDefinition(BEFORE_STEP, method, "", 0, lookup);
        definition.execute(state);
        assertTrue(invoked);
    }

    @BeforeStep
    public void step_hook_single_argument(Scenario scenario) {
        invoked = true;
    }

    @Test
    void can_create_step_hook_with_scenario_and_step_arguments() throws Throwable {
        Method method = JavaHookDefinitionTest.class.getMethod("step_hook_two_arguments", Scenario.class, Step.class);
        JavaHookDefinition definition = new JavaHookDefinition(BEFORE_STEP, method, "", 0, lookup);
        definition.execute(state);
        assertTrue(invoked);
    }

    @BeforeStep
    public void step_hook_two_arguments(Scenario scenario, Step step) {
        invoked = true;
    }

    @Test
    void fails_if_step_hook_has_invalid_first_parameter() throws NoSuchMethodException {
        Method method = JavaHookDefinitionTest.class.getMethod("step_hook_invalid_first_param", String.class);
        InvalidMethodSignatureException exception = assertThrows(
            InvalidMethodSignatureException.class,
            () -> new JavaHookDefinition(BEFORE_STEP, method, "", 0, lookup));
        assertThat(exception.getMessage(), startsWith("""
                A @BeforeStep annotated method must have one of these signatures:
                 * public void hook()
                 * public void hook(io.cucumber.java.Scenario scenario)
                 * public void hook(io.cucumber.java.Scenario scenario, io.cucumber.java.Step step)
                """));
    }

    public void step_hook_invalid_first_param(String badType) {
    }

    @Test
    void fails_if_step_hook_has_invalid_second_parameter() throws NoSuchMethodException {
        Method method = JavaHookDefinitionTest.class.getMethod("step_hook_invalid_second_param", Scenario.class,
            String.class);
        InvalidMethodSignatureException exception = assertThrows(
            InvalidMethodSignatureException.class,
            () -> new JavaHookDefinition(BEFORE_STEP, method, "", 0, lookup));
        assertThat(exception.getMessage(), startsWith("""
                A @BeforeStep annotated method must have one of these signatures:
                 * public void hook()
                 * public void hook(io.cucumber.java.Scenario scenario)
                 * public void hook(io.cucumber.java.Scenario scenario, io.cucumber.java.Step step)
                """));
    }

    public void step_hook_invalid_second_param(Scenario scenario, String badType) {
    }

    @Test
    void fails_if_step_hook_has_too_many_parameters() throws NoSuchMethodException {
        Method method = JavaHookDefinitionTest.class.getMethod("step_hook_too_many_params", Scenario.class, Step.class,
            String.class);
        InvalidMethodSignatureException exception = assertThrows(
            InvalidMethodSignatureException.class,
            () -> new JavaHookDefinition(BEFORE_STEP, method, "", 0, lookup));
        assertThat(exception.getMessage(), startsWith("""
                A @BeforeStep annotated method must have one of these signatures:
                """));
    }

    public void step_hook_too_many_params(Scenario scenario, Step step, String extra) {
    }

    // Verify error messages use correct hook type annotation

    @Test
    void after_hook_error_message_references_after_annotation() throws NoSuchMethodException {
        Method method = JavaHookDefinitionTest.class.getMethod("invalid_parameter", String.class);
        InvalidMethodSignatureException exception = assertThrows(
            InvalidMethodSignatureException.class,
            () -> new JavaHookDefinition(AFTER, method, "", 0, lookup));
        assertThat(exception.getMessage(), startsWith("A @After annotated method must have one of these signatures:"));
    }

    @Test
    void after_step_hook_error_message_references_after_step_annotation() throws NoSuchMethodException {
        Method method = JavaHookDefinitionTest.class.getMethod("step_hook_invalid_first_param", String.class);
        InvalidMethodSignatureException exception = assertThrows(
            InvalidMethodSignatureException.class,
            () -> new JavaHookDefinition(AFTER_STEP, method, "", 0, lookup));
        assertThat(exception.getMessage(),
            startsWith("A @AfterStep annotated method must have one of these signatures:"));
    }

}
