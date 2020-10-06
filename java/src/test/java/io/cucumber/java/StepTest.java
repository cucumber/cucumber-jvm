package io.cucumber.java;

import io.cucumber.core.backend.TestCaseState;
import io.cucumber.plugin.event.HookTestStep;
import io.cucumber.plugin.event.Location;
import io.cucumber.plugin.event.PickleStepTestStep;
import io.cucumber.plugin.event.StepArgument;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StepTest {

    @Mock
    TestCaseState testCaseState;

    @Mock
    HookTestStep hookTestStep;

    @Mock
    PickleStepTestStep pickleStepTestStep;

    @Test
    void createTestCaseStateSuccess() {
        when(testCaseState.getCurrentTestStep()).thenReturn(Optional.of(hookTestStep));
        when(hookTestStep.getRelatedTestStep()).thenReturn(pickleStepTestStep);
        io.cucumber.plugin.event.Step mockStepDelegate = mock(io.cucumber.plugin.event.Step.class);
        when(pickleStepTestStep.getStep()).thenReturn(mockStepDelegate);
        when(mockStepDelegate.getArgument()).thenReturn(mock(StepArgument.class));
        when(mockStepDelegate.getKeyword()).thenReturn("Given");
        when(mockStepDelegate.getText()).thenReturn("text");
        when(mockStepDelegate.getLine()).thenReturn(1);
        when(mockStepDelegate.getLocation()).thenReturn(new Location(1, 2));
        Step step = new Step(testCaseState);
        assertThat(step.delegate, is(notNullValue()));
        assertThat(step.getArgument(), is(equalTo(mockStepDelegate.getArgument())));
        assertThat(step.getKeyword(), is(equalTo(mockStepDelegate.getKeyword())));
        assertThat(step.getText(), is(equalTo(mockStepDelegate.getText())));
        assertThat(step.getLine(), is(equalTo(mockStepDelegate.getLine())));
        assertThat(step.getLocation(), is(equalTo(mockStepDelegate.getLocation())));
    }

    @Test
    void testCaseStateShouldHaveCurrentTestStep() {
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> new Step(testCaseState));
        assertThat(exception.getMessage(), startsWith("No current TestStep was found in TestCaseState"));
    }

    @Test
    void testCaseStateShouldHaveCurrentStepEmpty() {
        when(testCaseState.getCurrentTestStep()).thenReturn(Optional.empty());
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> new Step(testCaseState));
        assertThat(exception.getMessage(), startsWith("No current TestStep was found in TestCaseState"));
    }

    @Test
    void testCaseStateShouldHaveCurrentHookTestStep() {
        when(testCaseState.getCurrentTestStep()).thenReturn(Optional.of(pickleStepTestStep));
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> new Step(testCaseState));
        assertThat(exception.getMessage(), startsWith("Current TestStep is not a HookTestStep"));
    }

    @Test
    void testCaseStateShouldHaveRelatedTestStep() {
        when(testCaseState.getCurrentTestStep()).thenReturn(Optional.of(hookTestStep));
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> new Step(testCaseState));
        assertThat(exception.getMessage(), startsWith("No related TestStep for current HookTestStep was found"));
    }

    @Test
    void testCaseStateShouldHavePickleStepTestStep() {
        when(testCaseState.getCurrentTestStep()).thenReturn(Optional.of(hookTestStep));
        when(hookTestStep.getRelatedTestStep()).thenReturn(hookTestStep);
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> new Step(testCaseState));
        assertThat(exception.getMessage(), startsWith("Related TestStep is not a PickleStepTestStep"));
    }
}
