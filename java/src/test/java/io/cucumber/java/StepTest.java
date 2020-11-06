package io.cucumber.java;

import io.cucumber.core.backend.PickleStep;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StepTest {

    @Mock
    PickleStep pickleStep;

    @Test
    void createTestCaseStateSuccess() {
        when(pickleStep.getArguments()).thenReturn(new Object[] { "test" });
        when(pickleStep.getKeyword()).thenReturn(PickleStep.Keyword.GIVEN);
        Step step = new Step(pickleStep);
        assertThat(step.delegate, is(notNullValue()));
        assertThat(step.getArguments(), is(equalTo(new Object[] { "test" })));
        assertThat(step.getKeyword(), is(equalTo(PickleStep.Keyword.GIVEN)));
    }

}
