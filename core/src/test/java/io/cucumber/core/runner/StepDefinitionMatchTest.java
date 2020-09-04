package io.cucumber.core.runner;

import io.cucumber.core.backend.CucumberBackendException;
import io.cucumber.core.backend.CucumberInvocationTargetException;
import io.cucumber.core.backend.Located;
import io.cucumber.core.backend.StepDefinition;
import io.cucumber.core.backend.StubStepDefinition;
import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.exception.CucumberException;
import io.cucumber.core.feature.TestFeatureParser;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.gherkin.Step;
import io.cucumber.core.runtime.TimeServiceEventBus;
import io.cucumber.core.stepexpression.Argument;
import io.cucumber.core.stepexpression.StepExpression;
import io.cucumber.core.stepexpression.StepExpressionFactory;
import io.cucumber.core.stepexpression.StepTypeRegistry;
import io.cucumber.cucumberexpressions.ParameterType;
import io.cucumber.datatable.DataTableType;
import io.cucumber.docstring.DocStringType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.time.Clock;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static java.util.Arrays.asList;
import static java.util.Locale.ENGLISH;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StepDefinitionMatchTest {

    private final StepTypeRegistry stepTypeRegistry = new StepTypeRegistry(ENGLISH);
    private final EventBus bus = new TimeServiceEventBus(Clock.systemUTC(), UUID::randomUUID);
    private final StepExpressionFactory stepExpressionFactory = new StepExpressionFactory(stepTypeRegistry, bus);
    private final UUID id = UUID.randomUUID();

    private final Located stubbedLocation = new Located() {
        @Override
        public boolean isDefinedAt(StackTraceElement stackTraceElement) {
            return false;
        }

        @Override
        public String getLocation() {
            return "{stubbed location}";
        }
    };

    @Test
    void executes_a_step() throws Throwable {
        Feature feature = TestFeatureParser.parse("" +
                "Feature: Test feature\n" +
                "  Scenario: Test scenario\n" +
                "     Given I have 4 cukes in my belly\n");
        Step step = feature.getPickles().get(0).getSteps().get(0);

        StepDefinition stepDefinition = new StubStepDefinition("I have {int} cukes in my belly", Integer.class);
        StepExpression expression = stepExpressionFactory.createExpression(stepDefinition);
        CoreStepDefinition coreStepDefinition = new CoreStepDefinition(id, stepDefinition, expression);
        List<Argument> arguments = coreStepDefinition.matchedArguments(step);
        StepDefinitionMatch stepDefinitionMatch = new PickleStepDefinitionMatch(arguments, stepDefinition, null, step);
        stepDefinitionMatch.runStep(null);
    }

    @Test
    void throws_arity_mismatch_exception_when_there_are_fewer_parameters_than_arguments() {
        Feature feature = TestFeatureParser.parse("" +
                "Feature: Test feature\n" +
                "  Scenario: Test scenario\n" +
                "     Given I have 4 cukes in my belly\n");
        Step step = feature.getPickles().get(0).getSteps().get(0);

        StepDefinition stepDefinition = new StubStepDefinition("I have {int} cukes in my belly");
        StepExpression expression = stepExpressionFactory.createExpression(stepDefinition);
        CoreStepDefinition coreStepDefinition = new CoreStepDefinition(id, stepDefinition, expression);
        List<Argument> arguments = coreStepDefinition.matchedArguments(step);
        StepDefinitionMatch stepDefinitionMatch = new PickleStepDefinitionMatch(arguments, stepDefinition, null, step);

        Executable testMethod = () -> stepDefinitionMatch.runStep(null);
        CucumberException actualThrown = assertThrows(CucumberException.class, testMethod);
        assertThat("Unexpected exception message", actualThrown.getMessage(), is(equalTo(
            "Step [I have {int} cukes in my belly] is defined with 0 parameters at '{stubbed location with details}'.\n"
                    +
                    "However, the gherkin step has 1 arguments:\n" +
                    " * 4\n" +
                    "Step text: I have 4 cukes in my belly")));
    }

    @Test
    void throws_arity_mismatch_exception_when_there_are_fewer_parameters_than_arguments_with_data_table() {
        Feature feature = TestFeatureParser.parse("" +
                "Feature: Test feature\n" +
                "  Scenario: Test scenario\n" +
                "     Given I have 4 cukes in my belly\n" +
                "       | A | B | \n" +
                "       | C | D | \n");
        Step step = feature.getPickles().get(0).getSteps().get(0);

        StepDefinition stepDefinition = new StubStepDefinition("I have {int} cukes in my belly");
        StepExpression expression = stepExpressionFactory.createExpression(stepDefinition);
        CoreStepDefinition coreStepDefinition = new CoreStepDefinition(id, stepDefinition, expression);
        List<Argument> arguments = coreStepDefinition.matchedArguments(step);
        PickleStepDefinitionMatch stepDefinitionMatch = new PickleStepDefinitionMatch(arguments, stepDefinition, null,
            step);

        Executable testMethod = () -> stepDefinitionMatch.runStep(null);
        CucumberException actualThrown = assertThrows(CucumberException.class, testMethod);
        assertThat("Unexpected exception message", actualThrown.getMessage(), is(equalTo(
            "Step [I have {int} cukes in my belly] is defined with 0 parameters at '{stubbed location with details}'.\n"
                    +
                    "However, the gherkin step has 2 arguments:\n" +
                    " * 4\n" +
                    " * Table:\n" +
                    "      | A | B |\n" +
                    "      | C | D |\n" +
                    "\n" +
                    "Step text: I have 4 cukes in my belly")));
    }

    @Test
    void throws_arity_mismatch_exception_when_there_are_more_parameters_than_arguments() {
        Feature feature = TestFeatureParser.parse("" +
                "Feature: Test feature\n" +
                "  Scenario: Test scenario\n" +
                "     Given I have 4 cukes in my belly\n" +
                "       | A | B | \n" +
                "       | C | D | \n");
        Step step = feature.getPickles().get(0).getSteps().get(0);

        StepDefinition stepDefinition = new StubStepDefinition("I have {int} cukes in my belly", Integer.TYPE,
            Short.TYPE, List.class);
        StepExpression expression = stepExpressionFactory.createExpression(stepDefinition);
        CoreStepDefinition coreStepDefinition = new CoreStepDefinition(id, stepDefinition, expression);
        List<Argument> arguments = coreStepDefinition.matchedArguments(step);
        PickleStepDefinitionMatch stepDefinitionMatch = new PickleStepDefinitionMatch(arguments, stepDefinition, null,
            step);

        Executable testMethod = () -> stepDefinitionMatch.runStep(null);
        CucumberException actualThrown = assertThrows(CucumberException.class, testMethod);
        assertThat("Unexpected exception message", actualThrown.getMessage(), is(equalTo(
            "Step [I have {int} cukes in my belly] is defined with 3 parameters at '{stubbed location with details}'.\n"
                    +
                    "However, the gherkin step has 2 arguments:\n" +
                    " * 4\n" +
                    " * Table:\n" +
                    "      | A | B |\n" +
                    "      | C | D |\n" +
                    "\n" +
                    "Step text: I have 4 cukes in my belly")));
    }

    @Test
    void throws_arity_mismatch_exception_when_there_are_more_parameters_and_no_arguments() {
        Feature feature = TestFeatureParser.parse("" +
                "Feature: Test feature\n" +
                "  Scenario: Test scenario\n" +
                "     Given I have cukes in my belly\n");
        Step step = feature.getPickles().get(0).getSteps().get(0);
        StepDefinition stepDefinition = new StubStepDefinition("I have cukes in my belly", Integer.TYPE, Short.TYPE,
            List.class);
        StepExpression expression = stepExpressionFactory.createExpression(stepDefinition);
        CoreStepDefinition coreStepDefinition = new CoreStepDefinition(id, stepDefinition, expression);
        List<Argument> arguments = coreStepDefinition.matchedArguments(step);
        StepDefinitionMatch stepDefinitionMatch = new PickleStepDefinitionMatch(arguments, stepDefinition, null, step);

        Executable testMethod = () -> stepDefinitionMatch.runStep(null);
        CucumberException actualThrown = assertThrows(CucumberException.class, testMethod);
        assertThat("Unexpected exception message", actualThrown.getMessage(), is(equalTo(
            "Step [I have cukes in my belly] is defined with 3 parameters at '{stubbed location with details}'.\n" +
                    "However, the gherkin step has 0 arguments.\n" +
                    "Step text: I have cukes in my belly")));
    }

    @Test
    void throws_register_type_in_configuration_exception_when_there_is_no_data_table_type_defined() {
        Feature feature = TestFeatureParser.parse("file:test.feature", "" +
                "Feature: Test feature\n" +
                "  Scenario: Test scenario\n" +
                "     Given I have a data table\n" +
                "       | A | \n");
        Step step = feature.getPickles().get(0).getSteps().get(0);

        StepDefinition stepDefinition = new StubStepDefinition(
            "I have a data table",
            UndefinedDataTableType.class);
        StepExpression expression = stepExpressionFactory.createExpression(stepDefinition);
        CoreStepDefinition coreStepDefinition = new CoreStepDefinition(id, stepDefinition, expression);
        List<Argument> arguments = coreStepDefinition.matchedArguments(step);
        StepDefinitionMatch stepDefinitionMatch = new PickleStepDefinitionMatch(
            arguments,
            stepDefinition,
            URI.create("file:path/to.feature"),
            step);

        Executable testMethod = () -> stepDefinitionMatch.runStep(null);
        CucumberException actualThrown = assertThrows(CucumberException.class, testMethod);
        assertThat("Unexpected exception message", actualThrown.getMessage(), is(equalTo(
            "Could not convert arguments for step [I have a data table] defined at '{stubbed location with details}'.\n"
                    +
                    "It appears you did not register a data table type.")));
    }

    @Test
    void throws_could_not_convert_exception_for_transformer_and_capture_group_mismatch() {
        stepTypeRegistry.defineParameterType(new ParameterType<>(
            "itemQuantity",
            "(few|some|lots of) (cukes|gherkins)",
            ItemQuantity.class,
            (String s) -> null // Wrong number of capture groups
        ));

        Feature feature = TestFeatureParser.parse("" +
                "Feature: Test feature\n" +
                "  Scenario: Test scenario\n" +
                "     Given I have some cukes in my belly\n");
        Step step = feature.getPickles().get(0).getSteps().get(0);
        StepDefinition stepDefinition = new StubStepDefinition("I have {itemQuantity} in my belly", ItemQuantity.class);
        StepExpression expression = stepExpressionFactory.createExpression(stepDefinition);
        CoreStepDefinition coreStepDefinition = new CoreStepDefinition(id, stepDefinition, expression);
        List<Argument> arguments = coreStepDefinition.matchedArguments(step);
        StepDefinitionMatch stepDefinitionMatch = new PickleStepDefinitionMatch(arguments, stepDefinition, null, step);

        Executable testMethod = () -> stepDefinitionMatch.runStep(null);
        CucumberException actualThrown = assertThrows(CucumberException.class, testMethod);
        assertThat("Unexpected exception message", actualThrown.getMessage(), is(equalTo(
            "Could not convert arguments for step [I have {itemQuantity} in my belly] defined at '{stubbed location with details}'.")));
    }

    @Test
    void rethrows_target_invocation_exceptions_from_parameter_type() {
        RuntimeException userException = new RuntimeException();

        stepTypeRegistry.defineParameterType(new ParameterType<>(
            "itemQuantity",
            "(few|some|lots of) (cukes|gherkins)",
            ItemQuantity.class,
            (String[] s) -> {
                throw new CucumberInvocationTargetException(stubbedLocation,
                    new InvocationTargetException(userException));
            }));

        Feature feature = TestFeatureParser.parse("" +
                "Feature: Test feature\n" +
                "  Scenario: Test scenario\n" +
                "     Given I have some cukes in my belly\n");
        Step step = feature.getPickles().get(0).getSteps().get(0);
        StepDefinition stepDefinition = new StubStepDefinition("I have {itemQuantity} in my belly", ItemQuantity.class);
        StepExpression expression = stepExpressionFactory.createExpression(stepDefinition);
        CoreStepDefinition coreStepDefinition = new CoreStepDefinition(id, stepDefinition, expression);
        List<Argument> arguments = coreStepDefinition.matchedArguments(step);
        StepDefinitionMatch stepDefinitionMatch = new PickleStepDefinitionMatch(arguments, stepDefinition, null, step);

        Executable testMethod = () -> stepDefinitionMatch.runStep(null);
        RuntimeException actualThrown = assertThrows(RuntimeException.class, testMethod);
        assertThat(actualThrown, sameInstance(userException));
    }

    @Test
    void throws_could_not_convert_exception_for_singleton_table_dimension_mismatch() {
        Feature feature = TestFeatureParser.parse("" +
                "Feature: Test feature\n" +
                "  Scenario: Test scenario\n" +
                "     Given I have some cukes in my belly\n" +
                "       | 3 | \n" +
                "       | 14 | \n" +
                "       | 15 | \n");

        stepTypeRegistry.defineDataTableType(new DataTableType(ItemQuantity.class, ItemQuantity::new));

        Step step = feature.getPickles().get(0).getSteps().get(0);
        StepDefinition stepDefinition = new StubStepDefinition("I have some cukes in my belly", ItemQuantity.class);
        StepExpression expression = stepExpressionFactory.createExpression(stepDefinition);
        CoreStepDefinition coreStepDefinition = new CoreStepDefinition(id, stepDefinition, expression);
        List<Argument> arguments = coreStepDefinition.matchedArguments(step);
        StepDefinitionMatch stepDefinitionMatch = new PickleStepDefinitionMatch(arguments, stepDefinition, null, step);

        Executable testMethod = () -> stepDefinitionMatch.runStep(null);
        CucumberException actualThrown = assertThrows(CucumberException.class, testMethod);
        assertThat(actualThrown.getMessage(), is(equalTo(
            "Could not convert arguments for step [I have some cukes in my belly] defined at '{stubbed location with details}'.")));
    }

    @Test
    void rethrows_target_invocation_exceptions_from_data_table() {
        Feature feature = TestFeatureParser.parse("" +
                "Feature: Test feature\n" +
                "  Scenario: Test scenario\n" +
                "     Given I have some cukes in my belly\n" +
                "       | 3 | \n" +
                "       | 14 | \n" +
                "       | 15 | \n");
        RuntimeException userException = new RuntimeException();

        stepTypeRegistry.defineDataTableType(new DataTableType(
            ItemQuantity.class,
            (String cell) -> {
                throw new CucumberInvocationTargetException(stubbedLocation,
                    new InvocationTargetException(userException));
            }));

        Step step = feature.getPickles().get(0).getSteps().get(0);
        StepDefinition stepDefinition = new StubStepDefinition("I have some cukes in my belly", ItemQuantity.class);
        StepExpression expression = stepExpressionFactory.createExpression(stepDefinition);
        CoreStepDefinition coreStepDefinition = new CoreStepDefinition(id, stepDefinition, expression);
        List<Argument> arguments = coreStepDefinition.matchedArguments(step);
        StepDefinitionMatch stepDefinitionMatch = new PickleStepDefinitionMatch(arguments, stepDefinition, null, step);

        Executable testMethod = () -> stepDefinitionMatch.runStep(null);
        RuntimeException actualThrown = assertThrows(RuntimeException.class, testMethod);
        assertThat(actualThrown, sameInstance(userException));
    }

    @Test
    void throws_could_not_convert_exception_for_docstring() {
        Feature feature = TestFeatureParser.parse("" +
                "Feature: Test feature\n" +
                "  Scenario: Test scenario\n" +
                "     Given I have some cukes in my belly\n" +
                "       \"\"\"doc\n" +
                "        converting this should throw an exception\n" +
                "       \"\"\"\n");

        stepTypeRegistry.defineDocStringType(new DocStringType(ItemQuantity.class, "doc", content -> {
            throw new IllegalArgumentException(content);
        }));

        Step step = feature.getPickles().get(0).getSteps().get(0);
        StepDefinition stepDefinition = new StubStepDefinition("I have some cukes in my belly", ItemQuantity.class);
        StepExpression expression = stepExpressionFactory.createExpression(stepDefinition);
        CoreStepDefinition coreStepDefinition = new CoreStepDefinition(id, stepDefinition, expression);
        List<Argument> arguments = coreStepDefinition.matchedArguments(step);
        StepDefinitionMatch stepDefinitionMatch = new PickleStepDefinitionMatch(arguments, stepDefinition, null, step);

        Executable testMethod = () -> stepDefinitionMatch.runStep(null);
        CucumberException actualThrown = assertThrows(CucumberException.class, testMethod);
        assertThat(actualThrown.getMessage(), is(equalTo(
            "Could not convert arguments for step [I have some cukes in my belly] defined at '{stubbed location with details}'.")));
    }

    @Test
    void rethrows_target_invocation_exception_for_docstring() {
        RuntimeException userException = new RuntimeException();

        Feature feature = TestFeatureParser.parse("" +
                "Feature: Test feature\n" +
                "  Scenario: Test scenario\n" +
                "     Given I have some cukes in my belly\n" +
                "       \"\"\"doc\n" +
                "        converting this should throw an exception\n" +
                "       \"\"\"\n");

        stepTypeRegistry.defineDocStringType(new DocStringType(ItemQuantity.class, "doc", content -> {
            throw new CucumberInvocationTargetException(stubbedLocation, new InvocationTargetException(userException));
        }));

        Step step = feature.getPickles().get(0).getSteps().get(0);
        StepDefinition stepDefinition = new StubStepDefinition("I have some cukes in my belly", ItemQuantity.class);
        StepExpression expression = stepExpressionFactory.createExpression(stepDefinition);
        CoreStepDefinition coreStepDefinition = new CoreStepDefinition(id, stepDefinition, expression);
        List<Argument> arguments = coreStepDefinition.matchedArguments(step);
        StepDefinitionMatch stepDefinitionMatch = new PickleStepDefinitionMatch(arguments, stepDefinition, null, step);

        Executable testMethod = () -> stepDefinitionMatch.runStep(null);
        RuntimeException actualThrown = assertThrows(RuntimeException.class, testMethod);
        assertThat(actualThrown, sameInstance(userException));
    }

    @Test
    void throws_could_not_invoke_argument_conversion_when_argument_could_not_be_got() {
        Feature feature = TestFeatureParser.parse("file:test.feature", "" +
                "Feature: Test feature\n" +
                "  Scenario: Test scenario\n" +
                "     Given I have a data table\n" +
                "       | A | \n");
        Step step = feature.getPickles().get(0).getSteps().get(0);

        StepDefinition stepDefinition = new StubStepDefinition(
            "I have a data table",
            UndefinedDataTableType.class);
        List<Argument> arguments = Collections.singletonList(() -> {
            throw new CucumberBackendException("This exception is expected", new IllegalAccessException());
        });
        StepDefinitionMatch stepDefinitionMatch = new PickleStepDefinitionMatch(
            arguments,
            stepDefinition,
            URI.create("file:path/to.feature"),
            step);

        Executable testMethod = () -> stepDefinitionMatch.runStep(null);
        CucumberException actualThrown = assertThrows(CucumberException.class, testMethod);
        assertThat("Unexpected exception message", actualThrown.getMessage(), is(equalTo(
            "Could not convert arguments for step [I have a data table] defined at '{stubbed location with details}'.\n"
                    +
                    "It appears there was a problem with a hook or transformer definition.")));
    }

    @Test
    void throws_could_not_invoke_step_when_execution_failed_due_to_bad_methods() {
        Feature feature = TestFeatureParser.parse("file:test.feature", "" +
                "Feature: Test feature\n" +
                "  Scenario: Test scenario\n" +
                "     Given I have a data table\n" +
                "       | A | \n" +
                "       | B | \n");
        Step step = feature.getPickles().get(0).getSteps().get(0);

        StepDefinition stepDefinition = new StubStepDefinition(
            "I have a data table",
            new CucumberBackendException("This exception is expected!", new IllegalAccessException()),
            String.class,
            String.class);

        List<Argument> arguments = asList(
            () -> "mocked table cell",
            () -> "mocked table cell");
        StepDefinitionMatch stepDefinitionMatch = new PickleStepDefinitionMatch(
            arguments,
            stepDefinition,
            URI.create("file:path/to.feature"),
            step);

        Executable testMethod = () -> stepDefinitionMatch.runStep(null);
        CucumberException actualThrown = assertThrows(CucumberException.class, testMethod);
        assertThat("Unexpected exception message", actualThrown.getMessage(), is(equalTo(
            "Could not invoke step [I have a data table] defined at '{stubbed location with details}'.\n" +
                    "It appears there was a problem with the step definition.\n" +
                    "The converted arguments types were (java.lang.String, java.lang.String)")));
    }

    @Test
    void throws_could_not_invoke_step_when_execution_failed_with_null_arguments() {
        Feature feature = TestFeatureParser.parse("file:test.feature", "" +
                "Feature: Test feature\n" +
                "  Scenario: Test scenario\n" +
                "     Given I have an null value\n");
        Step step = feature.getPickles().get(0).getSteps().get(0);

        StepDefinition stepDefinition = new StubStepDefinition(
            "I have an {word} value",
            new CucumberBackendException("This exception is expected!", new IllegalAccessException()),
            String.class);

        List<Argument> arguments = asList(
            () -> null);
        StepDefinitionMatch stepDefinitionMatch = new PickleStepDefinitionMatch(
            arguments,
            stepDefinition,
            URI.create("file:path/to.feature"),
            step);

        Executable testMethod = () -> stepDefinitionMatch.runStep(null);
        CucumberException actualThrown = assertThrows(CucumberException.class, testMethod);
        assertThat("Unexpected exception message", actualThrown.getMessage(), is(equalTo(
            "Could not invoke step [I have an {word} value] defined at '{stubbed location with details}'.\n" +
                    "It appears there was a problem with the step definition.\n" +
                    "The converted arguments types were (null)")));
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
