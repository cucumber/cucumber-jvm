package io.cucumber.java;

import io.cucumber.core.backend.Lookup;
import io.cucumber.core.backend.TestCaseState;
import io.cucumber.plugin.event.HookTestStep;
import io.cucumber.plugin.event.PickleStepTestStep;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

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

    @Mock
    private PickleStepTestStep pickleStepTestStep;

    @Mock
    private HookTestStep hookTestStep;

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
        Method method = JavaHookDefinitionTest.class.getMethod("scenario_argument", Scenario.class);
        JavaHookDefinition definition = new JavaHookDefinition(method, "", 0, lookup);
        definition.execute(state);
        assertTrue(invoked);
    }

    @Before
    public void scenario_argument(Scenario scenario) {
        invoked = true;
    }

    @Test
    void can_create_with_scenario_and_step_arguments() throws Throwable {
        mockValidStep();
        Method method = JavaHookDefinitionTest.class.getMethod("scenario_step_arguments", Scenario.class, Step.class);
        JavaHookDefinition definition = new JavaHookDefinition(method, "", 0, lookup);
        definition.execute(state);
        assertTrue(invoked);
    }

    @BeforeStep
    public void scenario_step_arguments(Scenario scenario, Step step) {
        invoked = true;
    }

    @Test
    void can_create_with_step_and_scenario_arguments() throws Throwable {
        mockValidStep();
        Method method = JavaHookDefinitionTest.class.getMethod("step_scenario_arguments", Step.class, Scenario.class);
        JavaHookDefinition definition = new JavaHookDefinition(method, "", 0, lookup);
        definition.execute(state);
        assertTrue(invoked);
    }

    @BeforeStep
    public void step_scenario_arguments(Step step, Scenario scenario) {
        invoked = true;
    }

    @Test
    void can_create_with_only_step_argument() throws Throwable {
        mockValidStep();
        Method method = JavaHookDefinitionTest.class.getMethod("beforestep_step_argument", Step.class);
        JavaHookDefinition definition = new JavaHookDefinition(method, "", 0, lookup);
        definition.execute(state);
        assertTrue(invoked);
    }

    @BeforeStep
    public void beforestep_step_argument(Step step) {
        invoked = true;
    }

    @Test
    void can_create_with_only_scenario_argument() throws Throwable {
        Method method = JavaHookDefinitionTest.class.getMethod("beforestep_scenario_argument", Scenario.class);
        JavaHookDefinition definition = new JavaHookDefinition(method, "", 0, lookup);
        definition.execute(state);
        assertTrue(invoked);
    }

    @BeforeStep
    public void beforestep_scenario_argument(Scenario scenario) {
        invoked = true;
    }

    @Test
    void fails_if_hook_argument_is_not_scenario_result() throws NoSuchMethodException {
        Method method = JavaHookDefinitionTest.class.getMethod("invalid_parameter", String.class);
        InvalidMethodSignatureException exception = assertThrows(
            InvalidMethodSignatureException.class,
            () -> new JavaHookDefinition(method, "", 0, lookup));
        assertThat(exception.getMessage(), startsWith("" +
                "A method annotated with Before or After must have one of these signatures:\n" +
                " * public void before_or_after(io.cucumber.java.Scenario scenario)\n" +
                " * public void before_or_after()\n" +
                "at io.cucumber.java.JavaHookDefinitionTest.invalid_parameter(java.lang.String"));
    }

    @Before
    public void invalid_parameter(String badType) {

    }

    @Test
    void fails_if_hook_argument_is_not_scenario_step_result() throws NoSuchMethodException {
        Method method = JavaHookDefinitionTest.class.getMethod("invalid_parameter_step", String.class);
        InvalidMethodSignatureException exception = assertThrows(
                InvalidMethodSignatureException.class,
                () -> new JavaHookDefinition(method, "", 0, lookup));
        assertThat(exception.getMessage(), startsWith("" +
                "A method annotated with BeforeStep or AfterStep must have one of these signatures:\n" +
                " * public void before_or_after_step(io.cucumber.java.Scenario scenario, io.cucumber.java.Step step)\n" +
                " * public void before_or_after_step(io.cucumber.java.Step step, io.cucumber.java.Scenario scenario)\n" +
                " * public void before_or_after_step(io.cucumber.java.Step step)\n" +
                " * public void before_or_after_step(io.cucumber.java.Scenario scenario)\n" +
                " * public void before_or_after_step()\n" +
                "at io.cucumber.java.JavaHookDefinitionTest.invalid_parameter_step(java.lang.String"));
    }

    @BeforeStep
    @AfterStep
    public void invalid_parameter_step(String badType) {

    }

    @Test
    void fails_if_generic_hook_argument_is_not_scenario_result() throws NoSuchMethodException {
        Method method = JavaHookDefinitionTest.class.getMethod("invalid_generic_parameter", List.class);
        assertThrows(
            InvalidMethodSignatureException.class,
            () -> new JavaHookDefinition(method, "", 0, lookup));
    }

    @Before
    public void invalid_generic_parameter(List<String> badType) {

    }

    @Test
    void fails_if_too_many_arguments() throws NoSuchMethodException {
        Method method = JavaHookDefinitionTest.class.getMethod("too_many_parameters", Scenario.class, String.class);
        assertThrows(
            InvalidMethodSignatureException.class,
            () -> new JavaHookDefinition(method, "", 0, lookup));
    }

    @Before
    public void too_many_parameters(Scenario arg1, String arg2) {

    }

    @Test
    void fails_if_step_hook_argument_for_non_step_hook() throws NoSuchMethodException {
        Method method = JavaHookDefinitionTest.class.getMethod("invalid_step_parameter", Step.class);
        assertThrows(
                InvalidMethodSignatureException.class,
                () -> new JavaHookDefinition(method, "", 0, lookup));
    }

    @Before
    public void invalid_step_parameter(Step step) {

    }

    @Test
    void fails_if_step_hook_argument_and_both_beforestep_and_before_annotations() throws NoSuchMethodException {
        Method method = JavaHookDefinitionTest.class.getMethod("double_annotated_with_invalid_step_parameter", Step.class);
        assertThrows(
                InvalidMethodSignatureException.class,
                () -> new JavaHookDefinition(method, "", 0, lookup));
    }

    @Before
    @BeforeStep
    public void double_annotated_with_invalid_step_parameter(Step step) {
        // Invalid because @Before cannot set Step
    }

    private void mockValidStep() {
        when(state.getCurrentTestStep()).thenReturn(Optional.of(hookTestStep));
        when(hookTestStep.getRelatedTestStep()).thenReturn(pickleStepTestStep);
    }

}
