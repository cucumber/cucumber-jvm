package io.cucumber.core.runner;

import cucumber.api.Scenario;
import gherkin.pickles.PickleLocation;
import gherkin.pickles.PickleStep;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.function.Executable;

import static java.util.Arrays.asList;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FailedStepInstantiationMatchTest {

    private FailedPickleStepInstantiationMatch match;

    @Before
    public void create_match() {
        PickleLocation location = mock(PickleLocation.class);
        when(location.getLine()).thenReturn(1);
        PickleStep step = mock(PickleStep.class);
        when(step.getLocations()).thenReturn(asList(location));
        when(step.getText()).thenReturn("step text");
        Exception exception = mock(Exception.class);
        StackTraceElement[] stackTrace = {new StackTraceElement("declaringClass", "methodName", "fileName", 1)};
        when(exception.getStackTrace()).thenReturn(stackTrace);
        match = new FailedPickleStepInstantiationMatch("uri", step, exception);
    }

    @Test
    public void throws_the_exception_passed_to_the_match_when_run() {
        final Executable testMethod = () -> match.runStep(mock(Scenario.class));
        final Exception expectedThrown = assertThrows(Exception.class, testMethod);
        assertThat(expectedThrown.getMessage(), is(nullValue()));
    }

    @Test
    public void throws_the_exception_passed_to_the_match_when_dry_run() {
        final Executable testMethod = () -> match.dryRunStep(mock(Scenario.class));
        final Exception expectedThrown = assertThrows(Exception.class, testMethod);
        assertThat(expectedThrown.getMessage(), is(nullValue()));
    }

}
