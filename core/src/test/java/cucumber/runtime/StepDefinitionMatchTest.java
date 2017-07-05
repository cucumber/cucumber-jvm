package cucumber.runtime;

import cucumber.deps.com.thoughtworks.xstream.annotations.XStreamConverter;
import cucumber.deps.com.thoughtworks.xstream.converters.basic.AbstractSingleValueConverter;
import cucumber.runtime.xstream.LocalizedXStreams;
import gherkin.pickles.PickleLocation;
import gherkin.pickles.PickleStep;
import gherkin.pickles.PickleString;
import gherkin.pickles.PickleTable;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class StepDefinitionMatchTest {
    private final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    private static final String ENGLISH = "en";

    @Test
    public void converts_numbers() throws Throwable {
        StepDefinition stepDefinition = mock(StepDefinition.class);
        when(stepDefinition.getParameterCount()).thenReturn(1);
        when(stepDefinition.getParameterType(0, String.class)).thenReturn(new ParameterInfo(Integer.TYPE, null, null,
                null));

        PickleStep stepWithoutDocStringOrTable = mock(PickleStep.class);
        when(stepWithoutDocStringOrTable.getArgument()).thenReturn(Collections.<gherkin.pickles.Argument>emptyList());

        StepDefinitionMatch stepDefinitionMatch = new StepDefinitionMatch(Arrays.asList(new Argument(0, "5")), stepDefinition, "some.feature", stepWithoutDocStringOrTable, new LocalizedXStreams(classLoader));
        stepDefinitionMatch.runStep(ENGLISH, null);
        verify(stepDefinition).execute(ENGLISH, new Object[]{5});
    }

    @Test
    public void converts_with_explicit_converter() throws Throwable {
        StepDefinition stepDefinition = mock(StepDefinition.class);
        when(stepDefinition.getParameterCount()).thenReturn(1);
        when(stepDefinition.getParameterType(0, String.class)).thenReturn(new ParameterInfo(Thing.class, null, null,
                null));

        PickleStep stepWithoutDocStringOrTable = mock(PickleStep.class);
        when(stepWithoutDocStringOrTable.getArgument()).thenReturn(Collections.<gherkin.pickles.Argument>emptyList());

        StepDefinitionMatch stepDefinitionMatch = new StepDefinitionMatch(Arrays.asList(new Argument(0, "the thing")), stepDefinition, "some.feature", stepWithoutDocStringOrTable, new LocalizedXStreams(classLoader));
        stepDefinitionMatch.runStep(ENGLISH, null);
        verify(stepDefinition).execute(ENGLISH, new Object[]{new Thing("the thing")});
    }

    @Test
    public void converts_doc_string_with_explicit_converter() throws Throwable {
        StepDefinition stepDefinition = mock(StepDefinition.class);
        when(stepDefinition.getParameterCount()).thenReturn(1);
        when(stepDefinition.getParameterType(0, String.class)).thenReturn(new ParameterInfo(Thing.class, null, null,
                null));

        PickleStep stepWithDocString = mock(PickleStep.class);
        PickleString docString = new PickleString(mock(PickleLocation.class), "the thing");
        when(stepWithDocString.getArgument()).thenReturn(asList((gherkin.pickles.Argument)docString));

        StepDefinitionMatch stepDefinitionMatch = new StepDefinitionMatch(new ArrayList<Argument>(), stepDefinition, "some.feature", stepWithDocString, new LocalizedXStreams(classLoader));
        stepDefinitionMatch.runStep(ENGLISH, null);
        verify(stepDefinition).execute(ENGLISH, new Object[]{new Thing("the thing")});
    }

    @XStreamConverter(ThingConverter.class)
    public static class Thing {
        public final String name;

        public Thing(String name) {
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Thing thing = (Thing) o;
            return name.equals(thing.name);
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }
    }

    public static class ThingConverter extends AbstractSingleValueConverter {
        @Override
        public boolean canConvert(Class type) {
            return Thing.class.equals(type);
        }

        @Override
        public Object fromString(String str) {
            return new Thing(str);
        }
    }

    @Test
    public void gives_nice_error_message_when_conversion_fails() throws Throwable {
        StepDefinition stepDefinition = mock(StepDefinition.class);
        when(stepDefinition.getParameterCount()).thenReturn(1);
        when(stepDefinition.getParameterType(0, String.class)).thenReturn(new ParameterInfo(Thang.class, null, null,
                null));

        PickleStep stepWithoutDocStringOrTable = mock(PickleStep.class);
        when(stepWithoutDocStringOrTable.getArgument()).thenReturn(Collections.<gherkin.pickles.Argument>emptyList());

        StepDefinitionMatch stepDefinitionMatch = new StepDefinitionMatch(Arrays.asList(new Argument(0, "blah")), stepDefinition, "some.feature", stepWithoutDocStringOrTable, new LocalizedXStreams(classLoader));
        try {

            stepDefinitionMatch.runStep(ENGLISH, null);
            fail();
        } catch (CucumberException expected) {
            assertEquals(
                    "Don't know how to convert \"blah\" into cucumber.runtime.StepDefinitionMatchTest$Thang.\n" +
                            "Try writing your own converter:\n" +
                            "\n" +
                            "@cucumber.deps.com.thoughtworks.xstream.annotations.XStreamConverter(ThangConverter.class)\n" +
                            "public class Thang {}\n",
                    expected.getMessage()
            );
        }
    }

    public static class Thang {

    }

    @Test
    public void can_have_doc_string_as_only_argument() throws Throwable {
        StepDefinition stepDefinition = mock(StepDefinition.class);
        when(stepDefinition.getParameterCount()).thenReturn(1);
        when(stepDefinition.getParameterType(0, String.class)).thenReturn(new ParameterInfo(String.class, null, null,
                null));

        PickleStep stepWithDocString = mock(PickleStep.class);
        PickleString docString = new PickleString(mock(PickleLocation.class), "HELLO");
        when(stepWithDocString.getArgument()).thenReturn(asList((gherkin.pickles.Argument)docString));

        StepDefinitionMatch stepDefinitionMatch = new StepDefinitionMatch(new ArrayList<Argument>(), stepDefinition, "some.feature", stepWithDocString, new LocalizedXStreams(classLoader));
        stepDefinitionMatch.runStep(ENGLISH, null);
        verify(stepDefinition).execute(ENGLISH, new Object[]{"HELLO"});
    }

    @Test
    public void can_have_doc_string_as_last_argument_among_many() throws Throwable {
        StepDefinition stepDefinition = mock(StepDefinition.class);
        when(stepDefinition.getParameterCount()).thenReturn(2);
        when(stepDefinition.getParameterType(0, String.class)).thenReturn(new ParameterInfo(Integer.TYPE, null, null,
                null));
        when(stepDefinition.getParameterType(1, String.class)).thenReturn(new ParameterInfo(String.class, null, null,
                null));

        PickleStep stepWithDocString = mock(PickleStep.class);
        PickleString docString = new PickleString(mock(PickleLocation.class), "HELLO");
        when(stepWithDocString.getArgument()).thenReturn(asList((gherkin.pickles.Argument)docString));

        StepDefinitionMatch stepDefinitionMatch = new StepDefinitionMatch(Arrays.asList(new Argument(0, "5")), stepDefinition, "some.feature", stepWithDocString, new LocalizedXStreams(classLoader));
        stepDefinitionMatch.runStep(ENGLISH, null);
        verify(stepDefinition).execute(ENGLISH, new Object[]{5, "HELLO"});
    }

    @Test
    public void throws_arity_mismatch_exception_when_there_are_fewer_parameters_than_arguments() throws Throwable {
        PickleStep step = new PickleStep("I have 4 cukes in my belly", Collections.<gherkin.pickles.Argument>emptyList(), asList(mock(PickleLocation.class)));

        StepDefinition stepDefinition = new StubStepDefinition(new Object(), Object.class.getMethod("toString"), "some pattern");
        StepDefinitionMatch stepDefinitionMatch = new StepDefinitionMatch(asList(new Argument(7, "4")), stepDefinition, null, step, new LocalizedXStreams(getClass().getClassLoader()));
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

        StepDefinition stepDefinition = new StubStepDefinition(new Object(), WithTwoParams.class.getMethod("withTwoParams", Integer.TYPE, Short.TYPE, List.class), "some pattern");
        StepDefinitionMatch stepDefinitionMatch = new StepDefinitionMatch(asList(new Argument(7, "4")), stepDefinition, null, step, new LocalizedXStreams(getClass().getClassLoader()));
        try {
            stepDefinitionMatch.runStep(ENGLISH, null);
            fail();
        } catch (CucumberException expected) {
            assertEquals("Arity mismatch: Step Definition 'withTwoParams' with pattern [some pattern] is declared with 3 parameters. However, the gherkin step has 2 arguments [4, Table:[]]. \n" +
                    "Step text: I have 4 cukes in my belly", expected.getMessage());
        }
    }
}
