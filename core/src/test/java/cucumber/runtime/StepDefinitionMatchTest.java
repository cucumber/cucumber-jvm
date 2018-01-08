package cucumber.runtime;

import io.cucumber.stepexpression.Argument;
import gherkin.pickles.PickleLocation;
import gherkin.pickles.PickleStep;
import gherkin.pickles.PickleTable;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class StepDefinitionMatchTest {
    private static final String ENGLISH = "en";

    @Test
    public void converts_numbers() throws Throwable {
        StepDefinition stepDefinition = mock(StepDefinition.class);
        when(stepDefinition.getParameterCount()).thenReturn(1);
        when(stepDefinition.getParameterType(0, String.class)).thenReturn(new ParameterInfo(Integer.TYPE));

        PickleStep stepWithoutDocStringOrTable = mock(PickleStep.class);
        when(stepWithoutDocStringOrTable.getArgument()).thenReturn(Collections.<gherkin.pickles.Argument>emptyList());

        Argument argument = new Argument() {
            @Override
            public Object getValue() {
                return 5;
            }
        };

        StepDefinitionMatch stepDefinitionMatch = new StepDefinitionMatch(singletonList(argument), stepDefinition, "some.feature", stepWithoutDocStringOrTable);
        stepDefinitionMatch.runStep(ENGLISH, null);
        verify(stepDefinition).execute(ENGLISH, new Object[]{5});
    }

    @Test
    public void throws_arity_mismatch_exception_when_there_are_fewer_parameters_than_arguments() throws Throwable {
        PickleStep step = new PickleStep("I have 4 cukes in my belly", Collections.<gherkin.pickles.Argument>emptyList(), asList(mock(PickleLocation.class)));

        Argument argument = new Argument() {
            @Override
            public Object getValue() {
                return 4;
            }

            @Override
            public String toString() {
                return String.valueOf(getValue());
            }
        };

        StepDefinition stepDefinition = new StubStepDefinition(new Object(), Object.class.getMethod("toString"), "some pattern");
        StepDefinitionMatch stepDefinitionMatch = new StepDefinitionMatch(singletonList(argument), stepDefinition, null, step);
        try {
            stepDefinitionMatch.runStep(ENGLISH, null);
            fail();
        } catch (CucumberException expected) {
            assertEquals("Arity mismatch: Step Definition 'toString' with pattern [some pattern] is declared with 0 parameters. However, the gherkin step has 1 arguments [4]. \n" +
                    "Step text: I have 4 cukes in my belly", expected.getMessage());
        }
    }

    public static class WithTwoParams {
        public void withTwoParams(int anInt, short aShort, List<String> strings) {
        }
    }

    @Test
    public void throws_arity_mismatch_exception_when_there_are_more_parameters_than_arguments() throws Throwable {
        PickleStep step = new PickleStep("I have 4 cukes in my belly", asList((gherkin.pickles.Argument)mock(PickleTable.class)), asList(mock(PickleLocation.class)));

        Argument argument = new Argument() {
            @Override
            public Object getValue() {
                return 4;
            }

            @Override
            public String toString() {
                return String.valueOf(getValue());
            }
        };

        StepDefinition stepDefinition = new StubStepDefinition(new Object(), WithTwoParams.class.getMethod("withTwoParams", Integer.TYPE, Short.TYPE, List.class), "some pattern");
        StepDefinitionMatch stepDefinitionMatch = new StepDefinitionMatch(singletonList(argument), stepDefinition, null, step);
        try {
            stepDefinitionMatch.runStep(ENGLISH, null);
            fail();
        } catch (CucumberException expected) {
            assertEquals("Arity mismatch: Step Definition 'withTwoParams' with pattern [some pattern] is declared with 3 parameters. However, the gherkin step has 1 arguments [4]. \n" +
                    "Step text: I have 4 cukes in my belly", expected.getMessage());
        }
    }
}
