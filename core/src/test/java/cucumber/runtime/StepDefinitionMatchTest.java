package cucumber.runtime;

import cucumber.runtime.converters.LocalizedXStreams;
import gherkin.I18n;
import gherkin.formatter.Argument;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.DocString;
import gherkin.formatter.model.Step;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class StepDefinitionMatchTest {
    private final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    private static final I18n ENGLISH = new I18n("en");

    @Test
    public void converts_numbers() throws Throwable {
        StepDefinition stepDefinition = mock(StepDefinition.class);
        Reporter reporter = mock(Reporter.class);
        List<ParameterType> parameterTypes = asList(new ParameterType(Integer.TYPE, null));
        when(stepDefinition.getParameterTypes()).thenReturn(parameterTypes);

        Step stepWithoutDocStringOrTable = mock(Step.class);
        when(stepWithoutDocStringOrTable.getDocString()).thenReturn(null);
        when(stepWithoutDocStringOrTable.getRows()).thenReturn(null);

        StepDefinitionMatch stepDefinitionMatch = new StepDefinitionMatch(Arrays.asList(new Argument(0, "5")), stepDefinition, "some.feature", stepWithoutDocStringOrTable, new LocalizedXStreams(classLoader));
        stepDefinitionMatch.runStep(ENGLISH);
        verify(stepDefinition).execute(ENGLISH, new Object[]{5});
    }

    @Test
    public void can_have_doc_string_as_only_argument() throws Throwable {
        StepDefinition stepDefinition = mock(StepDefinition.class);
        Reporter reporter = mock(Reporter.class);
        List<ParameterType> parameterTypes = asList(new ParameterType(String.class, null));
        when(stepDefinition.getParameterTypes()).thenReturn(parameterTypes);

        Step stepWithDocString = mock(Step.class);
        DocString docString = new DocString("test", "HELLO", 999);
        when(stepWithDocString.getDocString()).thenReturn(docString);
        when(stepWithDocString.getRows()).thenReturn(null);

        StepDefinitionMatch stepDefinitionMatch = new StepDefinitionMatch(new ArrayList<Argument>(), stepDefinition, "some.feature", stepWithDocString, new LocalizedXStreams(classLoader));
        stepDefinitionMatch.runStep(ENGLISH);
        verify(stepDefinition).execute(ENGLISH, new Object[]{"HELLO"});
    }

    @Test
    public void can_have_doc_string_as_last_argument_among_many() throws Throwable {
        StepDefinition stepDefinition = mock(StepDefinition.class);
        Reporter reporter = mock(Reporter.class);
        List<ParameterType> parameterTypes = asList(new ParameterType(Integer.TYPE, null), new ParameterType(String.class, null));
        when(stepDefinition.getParameterTypes()).thenReturn(parameterTypes);

        Step stepWithDocString = mock(Step.class);
        DocString docString = new DocString("test", "HELLO", 999);
        when(stepWithDocString.getDocString()).thenReturn(docString);
        when(stepWithDocString.getRows()).thenReturn(null);

        StepDefinitionMatch stepDefinitionMatch = new StepDefinitionMatch(Arrays.asList(new Argument(0, "5")), stepDefinition, "some.feature", stepWithDocString, new LocalizedXStreams(classLoader));
        stepDefinitionMatch.runStep(ENGLISH);
        verify(stepDefinition).execute(ENGLISH, new Object[]{5, "HELLO"});
    }

    @Test
    public void retrieve_step_name()
    {
        String theName = "name";

        Step step = mock(Step.class);
        when(step.getName()).thenReturn(theName);

        StepDefinitionMatch stepDefinitionMatch = new StepDefinitionMatch(null, mock(StepDefinition.class), null, step, null);

        String stepName = stepDefinitionMatch.getStepName();
        assertEquals(theName, stepName);
    }
}
