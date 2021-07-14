package io.cucumber.java;

import io.cucumber.core.backend.HookStep;
import io.cucumber.core.backend.Lookup;
import io.cucumber.core.backend.PickleStep;
import io.cucumber.core.backend.TestCaseState;
import org.junit.Ignore;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings({ "WeakerAccess" })
@ExtendWith({ MockitoExtension.class })
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
@Ignore
public class JavaStepHookDefinitionTest {

    private final Lookup lookup = new Lookup() {

        @Override
        @SuppressWarnings("unchecked")
        public <T> T getInstance(Class<T> glueClass) {
            return (T) JavaStepHookDefinitionTest.this;
        }
    };

    @Mock
    private TestCaseState state;

    @Mock
    private PickleStep pickleStepTestStep;

    @Mock
    private HookStep hookTestStep;

    private boolean invoked = false;

    @Test
    void can_create_with_no_argument() throws Throwable {
        Method method = JavaStepHookDefinitionTest.class.getMethod("no_arguments");
        JavaStepHookDefinition definition = new JavaStepHookDefinition(method, "", 0, lookup);
        definition.execute(state);
        assertTrue(invoked);
    }

    @BeforeStep
    public void no_arguments() {
        invoked = true;
    }

    @Test
    void can_create_with_single_scenario_argument() throws Throwable {
        Method method = JavaStepHookDefinitionTest.class.getMethod("scenario_argument", Scenario.class);
        JavaStepHookDefinition definition = new JavaStepHookDefinition(method, "", 0, lookup);
        definition.execute(state);
        assertTrue(invoked);
    }

    @BeforeStep
    public void scenario_argument(Scenario scenario) {
        invoked = true;
    }

    @Test
    void can_create_with_scenario_and_step_arguments() throws Throwable {
        mockValidStep();
        Method method = JavaStepHookDefinitionTest.class.getMethod("scenario_step_arguments", Scenario.class,
            Step.class);
        JavaStepHookDefinition definition = new JavaStepHookDefinition(method, "", 0, lookup);
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
        Method method = JavaStepHookDefinitionTest.class.getMethod("step_scenario_arguments", Step.class,
            Scenario.class);
        JavaStepHookDefinition definition = new JavaStepHookDefinition(method, "", 0, lookup);
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
        Method method = JavaStepHookDefinitionTest.class.getMethod("beforestep_step_argument", Step.class);
        JavaStepHookDefinition definition = new JavaStepHookDefinition(method, "", 0, lookup);
        definition.execute(state);
        assertTrue(invoked);
    }

    @BeforeStep
    public void beforestep_step_argument(Step step) {
        invoked = true;
    }

    @Test
    void can_create_with_only_scenario_argument() throws Throwable {
        Method method = JavaStepHookDefinitionTest.class.getMethod("beforestep_scenario_argument", Scenario.class);
        JavaStepHookDefinition definition = new JavaStepHookDefinition(method, "", 0, lookup);
        definition.execute(state);
        assertTrue(invoked);
    }

    @BeforeStep
    public void beforestep_scenario_argument(Scenario scenario) {
        invoked = true;
    }

    @Test
    void fails_if_hook_argument_is_not_scenario_result() throws NoSuchMethodException {
        Method method = JavaStepHookDefinitionTest.class.getMethod("invalid_parameter", String.class);
        InvalidMethodSignatureException exception = assertThrows(
            InvalidMethodSignatureException.class,
            () -> new JavaStepHookDefinition(method, "", 0, lookup));
        assertThat(exception.getMessage(), startsWith("" +
                "A method annotated with BeforeStep or AfterStep must have one of these signatures:\n" +
                " * public void before_or_after_step(io.cucumber.java.Scenario scenario, io.cucumber.java.Step step)\n" +
                " * public void before_or_after_step(io.cucumber.java.Step step, io.cucumber.java.Scenario scenario)\n" +
                " * public void before_or_after_step(io.cucumber.java.Scenario scenario)\n" +
                " * public void before_or_after_step(io.cucumber.java.Step step)\n" +
                " * public void before_or_after_step()"));
    }

    @BeforeStep
    public void invalid_parameter(String badType) {

    }

    @Test
    void fails_if_hook_argument_is_not_scenario_step_result() throws NoSuchMethodException {
        Method method = JavaStepHookDefinitionTest.class.getMethod("invalid_parameter_step", String.class);
        InvalidMethodSignatureException exception = assertThrows(
            InvalidMethodSignatureException.class,
            () -> new JavaStepHookDefinition(method, "", 0, lookup));
        assertThat(exception.getMessage(), startsWith("" +
                "A method annotated with BeforeStep or AfterStep must have one of these signatures:\n" +
                " * public void before_or_after_step(io.cucumber.java.Scenario scenario, io.cucumber.java.Step step)\n" +
                " * public void before_or_after_step(io.cucumber.java.Step step, io.cucumber.java.Scenario scenario)\n" +
                " * public void before_or_after_step(io.cucumber.java.Scenario scenario)\n" +
                " * public void before_or_after_step(io.cucumber.java.Step step)\n" +
                " * public void before_or_after_step()"));
    }

    @BeforeStep
    @AfterStep
    public void invalid_parameter_step(String badType) {

    }

    @Test
    void fails_if_generic_hook_argument_is_not_scenario_result() throws NoSuchMethodException {
        Method method = JavaStepHookDefinitionTest.class.getMethod("invalid_generic_parameter", List.class);
        assertThrows(
            InvalidMethodSignatureException.class,
            () -> new JavaStepHookDefinition(method, "", 0, lookup));
    }

    @Before
    public void invalid_generic_parameter(List<String> badType) {

    }

    @Test
    void fails_if_too_many_arguments() throws NoSuchMethodException {
        Method method = JavaStepHookDefinitionTest.class.getMethod("too_many_parameters", Scenario.class, String.class);
        assertThrows(
            InvalidMethodSignatureException.class,
            () -> new JavaStepHookDefinition(method, "", 0, lookup));
    }

    @Before
    public void too_many_parameters(Scenario arg1, String arg2) {

    }

    private void mockValidStep() {
        when(state.getCurrentTestStep()).thenReturn(Optional.of(hookTestStep));
        when(hookTestStep.getRelatedStep()).thenReturn(pickleStepTestStep);
    }

    @Test
    void fails_if_testcasestate_has_no_current_teststep() throws NoSuchMethodException {
        when(state.getCurrentTestStep()).thenReturn(Optional.empty());
        Method method = JavaStepHookDefinitionTest.class.getMethod("scenario_step_arguments", Scenario.class,
            Step.class);
        JavaStepHookDefinition javaStepHookDefinition = new JavaStepHookDefinition(method, "", 0, lookup);
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> javaStepHookDefinition.execute(state));
        assertThat(exception.getMessage(), startsWith("No current TestStep was found in TestCaseState"));
    }

    @Test
    void fails_if_testcase_has_no_hookstep_as_current_teststep() throws NoSuchMethodException {
        when(state.getCurrentTestStep()).thenReturn(Optional.of(mock(PickleStep.class)));
        Method method = JavaStepHookDefinitionTest.class.getMethod("scenario_step_arguments", Scenario.class,
            Step.class);
        JavaStepHookDefinition javaStepHookDefinition = new JavaStepHookDefinition(method, "", 0, lookup);
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> {
                javaStepHookDefinition.execute(state);
            });
        assertThat(exception.getMessage(), startsWith("Current TestStep should be a HookStep instead of"));
    }

    @Test
    void fails_if_testcase_current_hookstep_has_no_related_step() throws NoSuchMethodException {
        when(state.getCurrentTestStep()).thenReturn(Optional.of(mock(HookStep.class)));
        Method method = JavaStepHookDefinitionTest.class.getMethod("scenario_step_arguments", Scenario.class,
            Step.class);
        JavaStepHookDefinition javaStepHookDefinition = new JavaStepHookDefinition(method, "", 0, lookup);
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> javaStepHookDefinition.execute(state));
        assertThat(exception.getMessage(), startsWith("Current HookStep has no related PickleStep"));
    }

}
