package cucumber.runner;

import cucumber.runtime.StepDefinition;
import cucumber.runtime.StepDefinitionMatch;
import cucumber.runtime.StubStepDefinition;
import io.cucumber.stepexpression.TypeRegistry;
import gherkin.pickles.PickleCell;
import gherkin.pickles.PickleLocation;
import gherkin.pickles.PickleRow;
import gherkin.pickles.PickleStep;
import gherkin.pickles.PickleTable;
import io.cucumber.cucumberexpressions.ParameterType;
import io.cucumber.cucumberexpressions.Transformer;
import io.cucumber.datatable.DataTableType;
import io.cucumber.datatable.TableCellTransformer;
import io.cucumber.stepexpression.Argument;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Locale.ENGLISH;
import static org.mockito.Mockito.mock;

public class StepDefinitionMatchTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    private final TypeRegistry typeRegistry = new TypeRegistry(ENGLISH);

    @Test
    public void executes_a_step() throws Throwable {
        PickleStep step = new PickleStep("I have 4 cukes in my belly", Collections.<gherkin.pickles.Argument>emptyList(), asList(mock(PickleLocation.class)));

        StepDefinition stepDefinition = new StubStepDefinition("I have {int} cukes in my belly", typeRegistry, Integer.class);
        List<Argument> arguments = stepDefinition.matchedArguments(step);
        StepDefinitionMatch stepDefinitionMatch = new PickleStepDefinitionMatch(arguments, stepDefinition, null, step);
        stepDefinitionMatch.runStep(null);
    }

    @Test
    public void throws_arity_mismatch_exception_when_there_are_fewer_parameters_than_arguments() throws Throwable {
        PickleStep step = new PickleStep("I have 4 cukes in my belly", Collections.<gherkin.pickles.Argument>emptyList(), asList(mock(PickleLocation.class)));

        StepDefinition stepDefinition = new StubStepDefinition("I have {int} cukes in my belly", typeRegistry);
        List<Argument> arguments = stepDefinition.matchedArguments(step);

        StepDefinitionMatch stepDefinitionMatch = new PickleStepDefinitionMatch(arguments, stepDefinition, null, step);

        expectedException.expectMessage(
            "" +
                "Step [I have {int} cukes in my belly] is defined with 0 parameters at '{stubbed location with details}'.\n" +
                "However, the gherkin step has 1 arguments:\n" +
                " * 4\n" +
                "Step text: I have 4 cukes in my belly");
        stepDefinitionMatch.runStep(null);
    }

    @Test
    public void throws_arity_mismatch_exception_when_there_are_fewer_parameters_than_arguments_with_data_table() throws Throwable {
        PickleTable table = new PickleTable(
            asList(
                new PickleRow(asList(new PickleCell(mock(PickleLocation.class), "A"), new PickleCell(mock(PickleLocation.class), "B"))),
                new PickleRow(asList(new PickleCell(mock(PickleLocation.class), "C"), new PickleCell(mock(PickleLocation.class), "D")))
            )
        );

        PickleStep step = new PickleStep("I have 4 cukes in my belly", asList((gherkin.pickles.Argument) table), asList(mock(PickleLocation.class)));

        StepDefinition stepDefinition = new StubStepDefinition("I have {int} cukes in my belly", typeRegistry);
        List<Argument> arguments = stepDefinition.matchedArguments(step);
        PickleStepDefinitionMatch stepDefinitionMatch = new PickleStepDefinitionMatch(arguments, stepDefinition, null, step);

        expectedException.expectMessage(
            "" +
                "Step [I have {int} cukes in my belly] is defined with 0 parameters at '{stubbed location with details}'.\n" +
                "However, the gherkin step has 2 arguments:\n" +
                " * 4\n" +
                " * Table:\n" +
                "      | A | B |\n" +
                "      | C | D |\n" +
                "\n" +
                "Step text: I have 4 cukes in my belly");
        stepDefinitionMatch.runStep(null);
    }

    @Test
    public void throws_arity_mismatch_exception_when_there_are_more_parameters_than_arguments() throws Throwable {
        PickleStep step = new PickleStep("I have 4 cukes in my belly", asList((gherkin.pickles.Argument) mock(PickleTable.class)), asList(mock(PickleLocation.class)));
        StepDefinition stepDefinition = new StubStepDefinition("I have {int} cukes in my belly", typeRegistry, Integer.TYPE, Short.TYPE, List.class);
        List<Argument> arguments = stepDefinition.matchedArguments(step);
        PickleStepDefinitionMatch stepDefinitionMatch = new PickleStepDefinitionMatch(arguments, stepDefinition, null, step);
        expectedException.expectMessage("" +
            "Step [I have {int} cukes in my belly] is defined with 3 parameters at '{stubbed location with details}'.\n" +
            "However, the gherkin step has 2 arguments:\n" +
            " * 4\n" +
            " * Table:\n" +
            "\n" +
            "Step text: I have 4 cukes in my belly");
        stepDefinitionMatch.runStep(null);
    }


    @Test
    public void throws_arity_mismatch_exception_when_there_are_more_parameters_and_no_arguments() throws Throwable {
        PickleStep step = new PickleStep("I have cukes in my belly", Collections.<gherkin.pickles.Argument>emptyList(), asList(mock(PickleLocation.class)));
        StepDefinition stepDefinition = new StubStepDefinition("I have cukes in my belly", typeRegistry, Integer.TYPE, Short.TYPE, List.class);
        List<Argument> arguments = stepDefinition.matchedArguments(step);
        StepDefinitionMatch stepDefinitionMatch = new PickleStepDefinitionMatch(arguments, stepDefinition, null, step);
        expectedException.expectMessage("" +
            "Step [I have cukes in my belly] is defined with 3 parameters at '{stubbed location with details}'.\n" +
            "However, the gherkin step has 0 arguments.\n" +
            "Step text: I have cukes in my belly");
        stepDefinitionMatch.runStep(null);
    }

    @Test
    public void throws_register_type_in_configuration_exception_when_there_is_no_data_table_type_defined() throws Throwable {
        // Empty table maps to null and doesn't trigger a type check.
        PickleTable table = new PickleTable(singletonList(new PickleRow(singletonList(new PickleCell(mock(PickleLocation.class), "A")))));

        PickleStep step = new PickleStep("I have a datatable", asList((gherkin.pickles.Argument) table), asList(mock(PickleLocation.class)));
        StepDefinition stepDefinition = new StubStepDefinition("I have a datatable", typeRegistry, UndefinedDataTableType.class);
        List<Argument> arguments = stepDefinition.matchedArguments(step);

        StepDefinitionMatch stepDefinitionMatch = new PickleStepDefinitionMatch(arguments, stepDefinition, null, step);
        expectedException.expectMessage("" +
            "Could not convert arguments for step [I have a datatable] defined at '{stubbed location with details}'.\n" +
            "It appears you did not register a data table type. The details are in the stacktrace below.");
        stepDefinitionMatch.runStep(null);

    }

    @Test
    public void throws_could_not_convert_exception_for_transfomer_and_capture_group_mismatch() throws Throwable {
        typeRegistry.defineParameterType(new ParameterType<ItemQuantity>(
            "itemQuantity",
            "(few|some|lots of) (cukes|gherkins)",
            ItemQuantity.class,
            new Transformer<ItemQuantity>() {
                @Override
                public ItemQuantity transform(String s) throws Throwable {
                    return null;
                }
            }));

        PickleStep step = new PickleStep("I have some cukes in my belly", Collections.<gherkin.pickles.Argument>emptyList(), asList(mock(PickleLocation.class)));
        StepDefinition stepDefinition = new StubStepDefinition("I have {itemQuantity} in my belly", typeRegistry, ItemQuantity.class);
        List<Argument> arguments = stepDefinition.matchedArguments(step);

        StepDefinitionMatch stepDefinitionMatch = new PickleStepDefinitionMatch(arguments, stepDefinition, null, step);
        expectedException.expectMessage("" +
            "Could not convert arguments for step [I have {itemQuantity} in my belly] defined at '{stubbed location with details}'.\n" +
            "The details are in the stacktrace below."
        );
        stepDefinitionMatch.runStep(null);

    }

    @Test
    public void throws_could_not_convert_exception_for_singleton_table_dimension_mismatch() throws Throwable {
        PickleTable table = new PickleTable(
            asList(
                new PickleRow(asList(new PickleCell(mock(PickleLocation.class), "A"), new PickleCell(mock(PickleLocation.class), "B"))),
                new PickleRow(asList(new PickleCell(mock(PickleLocation.class), "C"), new PickleCell(mock(PickleLocation.class), "D")))
            )
        );

        typeRegistry.defineDataTableType(new DataTableType(
            ItemQuantity.class,
            new TableCellTransformer<ItemQuantity>() {
                @Override
                public ItemQuantity transform(String s) {
                    return new ItemQuantity(s);
                }
            }

        ));

        PickleStep step = new PickleStep("I have some cukes in my belly", singletonList((gherkin.pickles.Argument) table), asList(mock(PickleLocation.class)));
        StepDefinition stepDefinition = new StubStepDefinition("I have some cukes in my belly", typeRegistry, ItemQuantity.class);
        List<Argument> arguments = stepDefinition.matchedArguments(step);

        StepDefinitionMatch stepDefinitionMatch = new PickleStepDefinitionMatch(arguments, stepDefinition, null, step);
        expectedException.expectMessage("" +
            "Could not convert arguments for step [I have some cukes in my belly] defined at '{stubbed location with details}'.\n" +
            "The details are in the stacktrace below.");
        stepDefinitionMatch.runStep(null);

    }

    private static final class ItemQuantity {

        private final String s;

        public ItemQuantity(String s) {
            this.s = s;
        }

        @Override
        public String toString() {
            return s;
        }
    }

    private static final class UndefinedDataTableType {

    }
}
