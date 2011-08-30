package cucumber.runtime;

import cucumber.runtime.transformers.Transformers;
import gherkin.formatter.Argument;
import gherkin.formatter.model.Step;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static org.mockito.Mockito.*;

public class StepDefinitionMatchTest {
    @Test
    public void shouldConvertParameters() throws Throwable {
        List<Argument> arguments = Arrays.asList(new Argument(0, "5"));
        StepDefinition stepDefinition = mock(StepDefinition.class);
        Class<?>[] parameterTypes = {Integer.TYPE};
        when(stepDefinition.getParameterTypes()).thenReturn(parameterTypes);

        Step stepWithoutDocStringOrTable = mock(Step.class);
        when(stepWithoutDocStringOrTable.getDocString()).thenReturn(null);
        when(stepWithoutDocStringOrTable.getRows()).thenReturn(null);

        StepDefinitionMatch stepDefinitionMatch = new StepDefinitionMatch(arguments, stepDefinition, "some.feature", stepWithoutDocStringOrTable, new Transformers());
        stepDefinitionMatch.runStep(Locale.ENGLISH);
        Object[] args = {5};
        verify(stepDefinition).execute(args);
    }
}
