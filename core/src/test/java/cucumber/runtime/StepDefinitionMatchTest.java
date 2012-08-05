package cucumber.runtime;

import cucumber.runtime.converters.LocalizedXStreams;
import cucumber.runtime.xstream.annotations.XStreamConverter;
import cucumber.runtime.xstream.converters.basic.AbstractSingleValueConverter;
import gherkin.I18n;
import gherkin.formatter.Argument;
import gherkin.formatter.model.DataTableRow;
import gherkin.formatter.model.DocString;
import gherkin.formatter.model.Step;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class StepDefinitionMatchTest {
    private final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    private static final I18n ENGLISH = new I18n("en");

    @Test
    public void converts_numbers() throws Throwable {
        StepDefinition stepDefinition = mock(StepDefinition.class);
        when(stepDefinition.getParameterCount()).thenReturn(1);
        when(stepDefinition.getParameterType(0, String.class)).thenReturn(new ParameterType(Integer.TYPE, null, null));

        Step stepWithoutDocStringOrTable = mock(Step.class);
        when(stepWithoutDocStringOrTable.getDocString()).thenReturn(null);
        when(stepWithoutDocStringOrTable.getRows()).thenReturn(null);

        StepDefinitionMatch stepDefinitionMatch = new StepDefinitionMatch(Arrays.asList(new Argument(0, "5")), stepDefinition, "some.feature", stepWithoutDocStringOrTable, new LocalizedXStreams(classLoader));
        stepDefinitionMatch.runStep(ENGLISH);
        verify(stepDefinition).execute(ENGLISH, new Object[]{5});
    }

    @Test
    public void converts_with_explicit_converter() throws Throwable {
        StepDefinition stepDefinition = mock(StepDefinition.class);
        when(stepDefinition.getParameterCount()).thenReturn(1);
        when(stepDefinition.getParameterType(0, String.class)).thenReturn(new ParameterType(Thing.class, null, null));

        Step stepWithoutDocStringOrTable = mock(Step.class);
        when(stepWithoutDocStringOrTable.getDocString()).thenReturn(null);
        when(stepWithoutDocStringOrTable.getRows()).thenReturn(null);

        StepDefinitionMatch stepDefinitionMatch = new StepDefinitionMatch(Arrays.asList(new Argument(0, "the thing")), stepDefinition, "some.feature", stepWithoutDocStringOrTable, new LocalizedXStreams(classLoader));
        stepDefinitionMatch.runStep(ENGLISH);
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
        when(stepDefinition.getParameterType(0, String.class)).thenReturn(new ParameterType(Thang.class, null, null));

        Step stepWithoutDocStringOrTable = mock(Step.class);
        when(stepWithoutDocStringOrTable.getDocString()).thenReturn(null);
        when(stepWithoutDocStringOrTable.getRows()).thenReturn(null);

        StepDefinitionMatch stepDefinitionMatch = new StepDefinitionMatch(Arrays.asList(new Argument(0, "blah")), stepDefinition, "some.feature", stepWithoutDocStringOrTable, new LocalizedXStreams(classLoader));
        try {

            stepDefinitionMatch.runStep(ENGLISH);
            fail();
        } catch (CucumberException expected) {
            assertEquals(
                    "Don't know how to convert \"blah\" into cucumber.runtime.StepDefinitionMatchTest$Thang.\n" +
                            "Try writing your own converter:\n" +
                            "\n" +
                            "@cucumber.runtime.xstream.annotations.XStreamConverter(ThangConverter.class)\n" +
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
        when(stepDefinition.getParameterType(0, String.class)).thenReturn(new ParameterType(String.class, null, null));

        Step stepWithDocString = mock(Step.class);
        DocString docString = new DocString("text/plain", "HELLO", 999);
        when(stepWithDocString.getDocString()).thenReturn(docString);
        when(stepWithDocString.getRows()).thenReturn(null);

        StepDefinitionMatch stepDefinitionMatch = new StepDefinitionMatch(new ArrayList<Argument>(), stepDefinition, "some.feature", stepWithDocString, new LocalizedXStreams(classLoader));
        stepDefinitionMatch.runStep(ENGLISH);
        verify(stepDefinition).execute(ENGLISH, new Object[]{"HELLO"});
    }

    @Test
    public void can_have_doc_string_as_last_argument_among_many() throws Throwable {
        StepDefinition stepDefinition = mock(StepDefinition.class);
        when(stepDefinition.getParameterCount()).thenReturn(2);
        when(stepDefinition.getParameterType(0, String.class)).thenReturn(new ParameterType(Integer.TYPE, null, null));
        when(stepDefinition.getParameterType(1, String.class)).thenReturn(new ParameterType(String.class, null, null));

        Step stepWithDocString = mock(Step.class);
        DocString docString = new DocString("test", "HELLO", 999);
        when(stepWithDocString.getDocString()).thenReturn(docString);
        when(stepWithDocString.getRows()).thenReturn(null);

        StepDefinitionMatch stepDefinitionMatch = new StepDefinitionMatch(Arrays.asList(new Argument(0, "5")), stepDefinition, "some.feature", stepWithDocString, new LocalizedXStreams(classLoader));
        stepDefinitionMatch.runStep(ENGLISH);
        verify(stepDefinition).execute(ENGLISH, new Object[]{5, "HELLO"});
    }

    @Test
    public void throws_arity_mismatch_exception_when_there_are_fewer_parameters_than_arguments() throws Throwable {
        Step step = new Step(null, "Given ", "I have 4 cukes in my belly", 1, null, null);

        StepDefinition stepDefinition = new StubStepDefinition(new Object(), Object.class.getMethod("toString"), "some pattern");
        StepDefinitionMatch stepDefinitionMatch = new StepDefinitionMatch(asList(new Argument(7, "4")), stepDefinition, null, step, new LocalizedXStreams(getClass().getClassLoader()));
        try {
            stepDefinitionMatch.runStep(new I18n("en"));
            fail();
        } catch (CucumberException expected) {
            assertEquals("Arity mismatch: Step Definition 'toString' with pattern [some pattern] is declared with 0 parameters. However, the gherkin step has 1 arguments [4]. \n" +
                    "Step: Given I have 4 cukes in my belly", expected.getMessage());
        }
    }

    public static class WithTwoParams {
        public void withTwoParams(int anInt, short aShort, List<String> strings) {}
    }

    @Test
    public void throws_arity_mismatch_exception_when_there_are_more_parameters_than_arguments() throws Throwable {
        Step step = new Step(null, "Given ", "I have 4 cukes in my belly", 1, new ArrayList<DataTableRow>(), null);

        StepDefinition stepDefinition = new StubStepDefinition(new Object(), WithTwoParams.class.getMethod("withTwoParams", Integer.TYPE, Short.TYPE, List.class), "some pattern");
        StepDefinitionMatch stepDefinitionMatch = new StepDefinitionMatch(asList(new Argument(7, "4")), stepDefinition, null, step, new LocalizedXStreams(getClass().getClassLoader()));
        try {
            stepDefinitionMatch.runStep(new I18n("en"));
            fail();
        } catch (CucumberException expected) {
            assertEquals("Arity mismatch: Step Definition 'withTwoParams' with pattern [some pattern] is declared with 3 parameters. However, the gherkin step has 2 arguments [4, Table:[]]. \n" +
                    "Step: Given I have 4 cukes in my belly", expected.getMessage());
        }
    }
}
