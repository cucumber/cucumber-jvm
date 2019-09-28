package io.cucumber.core.runner;

import io.cucumber.core.backend.CucumberBackendException;
import io.cucumber.core.backend.StepDefinition;
import io.cucumber.core.exception.CucumberException;
import io.cucumber.core.feature.CucumberFeature;
import io.cucumber.core.feature.CucumberStep;
import io.cucumber.core.feature.TestFeatureParser;
import io.cucumber.core.runtime.StubStepDefinition;
import io.cucumber.core.stepexpression.Argument;
import io.cucumber.core.stepexpression.StepTypeRegistry;
import io.cucumber.cucumberexpressions.ParameterType;
import io.cucumber.datatable.DataTableType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.net.URI;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Locale.ENGLISH;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StepDefinitionMatchTest {

    private final StepTypeRegistry stepTypeRegistry = new StepTypeRegistry(ENGLISH);

    @Test
    void executes_a_step() throws Throwable {
        CucumberFeature feature = TestFeatureParser.parse("" +
            "Feature: Test feature\n" +
            "  Scenario: Test scenario\n" +
            "     Given I have 4 cukes in my belly\n"
        );
        CucumberStep step = feature.getPickles().get(0).getSteps().get(0);

        StepDefinition stepDefinition = new StubStepDefinition("I have {int} cukes in my belly", Integer.class);
        CoreStepDefinition coreStepDefinition = new CoreStepDefinition(stepDefinition, stepTypeRegistry);
        List<Argument> arguments = coreStepDefinition.matchedArguments(step);
        StepDefinitionMatch stepDefinitionMatch = new PickleStepDefinitionMatch(arguments, stepDefinition, null, step);
        stepDefinitionMatch.runStep(null);
    }

    @Test
    void throws_arity_mismatch_exception_when_there_are_fewer_parameters_than_arguments() {
        CucumberFeature feature = TestFeatureParser.parse("" +
            "Feature: Test feature\n" +
            "  Scenario: Test scenario\n" +
            "     Given I have 4 cukes in my belly\n"
        );
        CucumberStep step = feature.getPickles().get(0).getSteps().get(0);

        StepDefinition stepDefinition = new StubStepDefinition("I have {int} cukes in my belly");
        CoreStepDefinition coreStepDefinition = new CoreStepDefinition(stepDefinition, stepTypeRegistry);
        List<Argument> arguments = coreStepDefinition.matchedArguments(step);

        StepDefinitionMatch stepDefinitionMatch = new PickleStepDefinitionMatch(arguments, stepDefinition, null, step);

        Executable testMethod = () -> stepDefinitionMatch.runStep(null);
        CucumberException actualThrown = assertThrows(CucumberException.class, testMethod);
        assertThat("Unexpected exception message", actualThrown.getMessage(), is(equalTo(
            "Step [I have {int} cukes in my belly] is defined with 0 parameters at '{stubbed location with details}'.\n" +
                "However, the gherkin step has 1 arguments:\n" +
                " * 4\n" +
                "Step text: I have 4 cukes in my belly"
        )));
    }

    @Test
    void throws_arity_mismatch_exception_when_there_are_fewer_parameters_than_arguments_with_data_table() {
        CucumberFeature feature = TestFeatureParser.parse("" +
            "Feature: Test feature\n" +
            "  Scenario: Test scenario\n" +
            "     Given I have 4 cukes in my belly\n" +
            "       | A | B | \n" +
            "       | C | D | \n"
        );
        CucumberStep step = feature.getPickles().get(0).getSteps().get(0);

        StepDefinition stepDefinition = new StubStepDefinition("I have {int} cukes in my belly");
        CoreStepDefinition coreStepDefinition = new CoreStepDefinition(stepDefinition, stepTypeRegistry);
        List<Argument> arguments = coreStepDefinition.matchedArguments(step);
        PickleStepDefinitionMatch stepDefinitionMatch = new PickleStepDefinitionMatch(arguments, stepDefinition, null, step);

        Executable testMethod = () -> stepDefinitionMatch.runStep(null);
        CucumberException actualThrown = assertThrows(CucumberException.class, testMethod);
        assertThat("Unexpected exception message", actualThrown.getMessage(), is(equalTo(
            "Step [I have {int} cukes in my belly] is defined with 0 parameters at '{stubbed location with details}'.\n" +
                "However, the gherkin step has 2 arguments:\n" +
                " * 4\n" +
                " * Table:\n" +
                "      | A | B |\n" +
                "      | C | D |\n" +
                "\n" +
                "Step text: I have 4 cukes in my belly"
        )));
    }

    @Test
    void throws_arity_mismatch_exception_when_there_are_more_parameters_than_arguments() {
        CucumberFeature feature = TestFeatureParser.parse("" +
            "Feature: Test feature\n" +
            "  Scenario: Test scenario\n" +
            "     Given I have 4 cukes in my belly\n" +
            "       | A | B | \n" +
            "       | C | D | \n"
        );
        CucumberStep step = feature.getPickles().get(0).getSteps().get(0);

        StepDefinition stepDefinition = new StubStepDefinition("I have {int} cukes in my belly", Integer.TYPE, Short.TYPE, List.class);
        CoreStepDefinition coreStepDefinition = new CoreStepDefinition(stepDefinition, stepTypeRegistry);
        List<Argument> arguments = coreStepDefinition.matchedArguments(step);
        PickleStepDefinitionMatch stepDefinitionMatch = new PickleStepDefinitionMatch(arguments, stepDefinition, null, step);

        Executable testMethod = () -> stepDefinitionMatch.runStep(null);
        CucumberException actualThrown = assertThrows(CucumberException.class, testMethod);
        assertThat("Unexpected exception message", actualThrown.getMessage(), is(equalTo(
            "Step [I have {int} cukes in my belly] is defined with 3 parameters at '{stubbed location with details}'.\n" +
                "However, the gherkin step has 2 arguments:\n" +
                " * 4\n" +
                " * Table:\n" +
                "      | A | B |\n" +
                "      | C | D |\n" +
                "\n" +
                "Step text: I have 4 cukes in my belly"
        )));
    }

    @Test
    void throws_arity_mismatch_exception_when_there_are_more_parameters_and_no_arguments() {
        CucumberFeature feature = TestFeatureParser.parse("" +
            "Feature: Test feature\n" +
            "  Scenario: Test scenario\n" +
            "     Given I have cukes in my belly\n"
        );
        CucumberStep step = feature.getPickles().get(0).getSteps().get(0);
        StepDefinition stepDefinition = new StubStepDefinition("I have cukes in my belly", Integer.TYPE, Short.TYPE, List.class);
        CoreStepDefinition coreStepDefinition = new CoreStepDefinition(stepDefinition, stepTypeRegistry);
        List<Argument> arguments = coreStepDefinition.matchedArguments(step);
        StepDefinitionMatch stepDefinitionMatch = new PickleStepDefinitionMatch(arguments, stepDefinition, null, step);

        Executable testMethod = () -> stepDefinitionMatch.runStep(null);
        CucumberException actualThrown = assertThrows(CucumberException.class, testMethod);
        assertThat("Unexpected exception message", actualThrown.getMessage(), is(equalTo(
            "Step [I have cukes in my belly] is defined with 3 parameters at '{stubbed location with details}'.\n" +
                "However, the gherkin step has 0 arguments.\n" +
                "Step text: I have cukes in my belly"
        )));
    }

    @Test
    void throws_register_type_in_configuration_exception_when_there_is_no_data_table_type_defined() {
        CucumberFeature feature = TestFeatureParser.parse("file:test.feature", "" +
            "Feature: Test feature\n" +
            "  Scenario: Test scenario\n" +
            "     Given I have a data table\n" +
            "       | A | \n"
        );
        CucumberStep step = feature.getPickles().get(0).getSteps().get(0);

        StepDefinition stepDefinition = new StubStepDefinition(
            "I have a data table",
            UndefinedDataTableType.class
        );
        CoreStepDefinition coreStepDefinition = new CoreStepDefinition(stepDefinition, stepTypeRegistry);
        List<Argument> arguments = coreStepDefinition.matchedArguments(step);

        StepDefinitionMatch stepDefinitionMatch = new PickleStepDefinitionMatch(
            arguments,
            stepDefinition,
            URI.create("file:path/to.feature"),
            step
        );

        Executable testMethod = () -> stepDefinitionMatch.runStep(null);
        CucumberException actualThrown = assertThrows(CucumberException.class, testMethod);
        assertThat("Unexpected exception message", actualThrown.getMessage(), is(equalTo(
            "Could not convert arguments for step [I have a data table] defined at '{stubbed location with details}'.\n" +
                "It appears you did not register a data table type. The details are in the stacktrace below."
        )));
    }

    @Test
    void throws_could_not_convert_exception_for_transfomer_and_capture_group_mismatch() {
        stepTypeRegistry.defineParameterType(new ParameterType<>(
            "itemQuantity",
            "(few|some|lots of) (cukes|gherkins)",
            ItemQuantity.class,
            (String s) -> null
        ));

        CucumberFeature feature = TestFeatureParser.parse("" +
            "Feature: Test feature\n" +
            "  Scenario: Test scenario\n" +
            "     Given I have some cukes in my belly\n"
        );
        CucumberStep step = feature.getPickles().get(0).getSteps().get(0);
        StepDefinition stepDefinition = new StubStepDefinition("I have {itemQuantity} in my belly", ItemQuantity.class);
        CoreStepDefinition coreStepDefinition = new CoreStepDefinition(stepDefinition, stepTypeRegistry);
        List<Argument> arguments = coreStepDefinition.matchedArguments(step);

        StepDefinitionMatch stepDefinitionMatch = new PickleStepDefinitionMatch(arguments, stepDefinition, null, step);

        Executable testMethod = () -> stepDefinitionMatch.runStep(null);
        CucumberException actualThrown = assertThrows(CucumberException.class, testMethod);
        assertThat("Unexpected exception message", actualThrown.getMessage(), is(equalTo(
            "Could not convert arguments for step [I have {itemQuantity} in my belly] defined at '{stubbed location with details}'.\n" +
                "The details are in the stacktrace below."
        )));
    }

    @Test
    void throws_could_not_convert_exception_for_singleton_table_dimension_mismatch() {
        CucumberFeature feature = TestFeatureParser.parse("" +
            "Feature: Test feature\n" +
            "  Scenario: Test scenario\n" +
            "     Given I have some cukes in my belly\n" +
            "       | 3 | \n" +
            "       | 14 | \n" +
            "       | 15 | \n"
        );

        stepTypeRegistry.defineDataTableType(new DataTableType(ItemQuantity.class, ItemQuantity::new));

        CucumberStep step = feature.getPickles().get(0).getSteps().get(0);
        StepDefinition stepDefinition = new StubStepDefinition("I have some cukes in my belly", ItemQuantity.class);
        CoreStepDefinition coreStepDefinition = new CoreStepDefinition(stepDefinition, stepTypeRegistry);
        List<Argument> arguments = coreStepDefinition.matchedArguments(step);

        StepDefinitionMatch stepDefinitionMatch = new PickleStepDefinitionMatch(arguments, stepDefinition, null, step);

        Executable testMethod = () -> stepDefinitionMatch.runStep(null);
        CucumberException actualThrown = assertThrows(CucumberException.class, testMethod);
        assertThat(actualThrown.getMessage(), is(equalTo(
            "Could not convert arguments for step [I have some cukes in my belly] defined at '{stubbed location with details}'.\n" +
                "The details are in the stacktrace below."
        )));
    }

    @Test
    void throws_could_not_invoke_argument_conversion_when_argument_could_not_be_got() {
        CucumberFeature feature = TestFeatureParser.parse("file:test.feature", "" +
            "Feature: Test feature\n" +
            "  Scenario: Test scenario\n" +
            "     Given I have a data table\n" +
            "       | A | \n"
        );
        CucumberStep step = feature.getPickles().get(0).getSteps().get(0);

        StepDefinition stepDefinition = new StubStepDefinition(
            "I have a data table",
            UndefinedDataTableType.class
        );
        List<Argument> arguments = Collections.singletonList(() -> {
            throw new CucumberBackendException("boom!", new IllegalAccessException());
        });

        StepDefinitionMatch stepDefinitionMatch = new PickleStepDefinitionMatch(
            arguments,
            stepDefinition,
            URI.create("file:path/to.feature"),
            step
        );

        Executable testMethod = () -> stepDefinitionMatch.runStep(null);
        CucumberException actualThrown = assertThrows(CucumberException.class, testMethod);
        assertThat("Unexpected exception message", actualThrown.getMessage(), is(equalTo(
            "Could not convert arguments for step [I have a data table] defined at '{stubbed location with details}'.\n" +
                "It appears there was a problem with a hook or transformer definition. The details are in the stacktrace below."
        )));
    }

    @Test
    void throws_could_not_invoke_step_when_execution_failed_due_to_bad_methods() {
        CucumberFeature feature = TestFeatureParser.parse("file:test.feature", "" +
            "Feature: Test feature\n" +
            "  Scenario: Test scenario\n" +
            "     Given I have a data table\n" +
            "       | A | \n" +
            "       | B | \n"
        );
        CucumberStep step = feature.getPickles().get(0).getSteps().get(0);

        StepDefinition stepDefinition = new StubStepDefinition(
            "I have a data table",
            new CucumberBackendException("boom!", new IllegalAccessException()),
            String.class,
            String.class
        );

        List<Argument> arguments = asList(
            () -> "mocked table cell",
            () -> "mocked table cell"
        );

        StepDefinitionMatch stepDefinitionMatch = new PickleStepDefinitionMatch(
            arguments,
            stepDefinition,
            URI.create("file:path/to.feature"),
            step
        );

        Executable testMethod = () -> stepDefinitionMatch.runStep(null);
        CucumberException actualThrown = assertThrows(CucumberException.class, testMethod);
        assertThat("Unexpected exception message", actualThrown.getMessage(), is(equalTo(
            "Could not invoke step [I have a data table] defined at '{stubbed location with details}'.\n" +
                "It appears there was a problem with the step definition.\n" +
                "The converted arguments types were (java.lang.String, java.lang.String)\n" +
                "\n" +
                "The details are in the stacktrace below."
        )));
    }

    private static final class ItemQuantity {

        private final String s;

        ItemQuantity(String s) {
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
