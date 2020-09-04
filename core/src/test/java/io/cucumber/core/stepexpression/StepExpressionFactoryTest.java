package io.cucumber.core.stepexpression;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.core.backend.StepDefinition;
import io.cucumber.core.backend.StubStepDefinition;
import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.exception.CucumberException;
import io.cucumber.core.runtime.TimeServiceEventBus;
import io.cucumber.cucumberexpressions.CucumberExpression;
import io.cucumber.datatable.DataTable;
import io.cucumber.datatable.DataTableType;
import io.cucumber.datatable.TableEntryTransformer;
import io.cucumber.datatable.TableTransformer;
import io.cucumber.docstring.DocString;
import io.cucumber.docstring.DocStringType;
import io.cucumber.messages.Messages;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableWithSize.iterableWithSize;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StepExpressionFactoryTest {

    private static final Type UNKNOWN_TYPE = Object.class;
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final EventBus bus = new TimeServiceEventBus(Clock.systemUTC(), UUID::randomUUID);
    private final StepTypeRegistry registry = new StepTypeRegistry(Locale.ENGLISH);
    private final StepExpressionFactory stepExpressionFactory = new StepExpressionFactory(registry, bus);
    private final List<List<String>> table = asList(asList("name", "amount", "unit"), asList("chocolate", "2", "tbsp"));
    private final List<List<String>> tableTransposed = asList(asList("name", "chocolate"), asList("amount", "2"),
        asList("unit", "tbsp"));

    @Test
    void creates_a_step_expression() {
        StepDefinition stepDefinition = new StubStepDefinition("Given a step");
        StepExpression expression = stepExpressionFactory.createExpression(stepDefinition);
        assertThat(expression.getSource(), is("Given a step"));
        assertThat(expression.getExpressionType(), is(CucumberExpression.class));
        assertThat(expression.match("Given a step"), is(emptyList()));
    }

    @Test
    void throws_for_unknown_parameter_types() {
        StepDefinition stepDefinition = new StubStepDefinition("Given a {unknownParameterType}");

        List<Messages.Envelope> events = new ArrayList<>();
        bus.registerHandlerFor(Messages.Envelope.class, events::add);

        CucumberException exception = assertThrows(
            CucumberException.class,
            () -> stepExpressionFactory.createExpression(stepDefinition));
        assertThat(exception.getMessage(), is("" +
                "Could not create a cucumber expression for 'Given a {unknownParameterType}'.\n" +
                "It appears you did not register a parameter type."

        ));
        assertThat(events, iterableWithSize(1));
        assertTrue(events.get(0).hasUndefinedParameterType());
    }

    @Test
    void table_expression_with_type_creates_table_from_table() {

        StepDefinition stepDefinition = new StubStepDefinition("Given some stuff:", DataTable.class);
        StepExpression expression = stepExpressionFactory.createExpression(stepDefinition);

        List<Argument> match = expression.match("Given some stuff:", table);

        DataTable dataTable = (DataTable) match.get(0).getValue();
        assertThat(dataTable.cells(), is(equalTo(table)));
    }

    @Test
    void table_expression_with_type_creates_single_ingredients_from_table() {

        registry.defineDataTableType(new DataTableType(Ingredient.class, beanMapper(registry)));
        StepDefinition stepDefinition = new StubStepDefinition("Given some stuff:", Ingredient.class);
        StepExpression expression = stepExpressionFactory.createExpression(stepDefinition);
        List<Argument> match = expression.match("Given some stuff:", tableTransposed);

        Ingredient ingredient = (Ingredient) match.get(0).getValue();
        assertThat(ingredient.name, is(equalTo("chocolate")));
    }

    private TableTransformer<Ingredient> beanMapper(final StepTypeRegistry registry) {
        return table -> {
            Map<String, String> tableRow = table.transpose().asMaps().get(0);
            return listBeanMapper(registry).transform(tableRow);
        };
    }

    private TableEntryTransformer<Ingredient> listBeanMapper(final StepTypeRegistry registry) {
        // Just pretend this is a bean mapper.
        return tableRow -> {
            Ingredient bean = new Ingredient();
            bean.amount = Integer.valueOf(tableRow.get("amount"));
            bean.name = tableRow.get("name");
            bean.unit = tableRow.get("unit");
            return bean;
        };
    }

    @SuppressWarnings("unchecked")
    @Test
    void table_expression_with_list_type_creates_list_of_ingredients_from_table() {

        registry.defineDataTableType(new DataTableType(Ingredient.class, listBeanMapper(registry)));

        StepDefinition stepDefinition = new StubStepDefinition("Given some stuff:", getTypeFromStepDefinition());
        StepExpression expression = stepExpressionFactory.createExpression(stepDefinition);
        List<Argument> match = expression.match("Given some stuff:", table);

        List<Ingredient> ingredients = (List<Ingredient>) match.get(0).getValue();
        Ingredient ingredient = ingredients.get(0);
        assertThat(ingredient.amount, is(equalTo(2)));
    }

    private Type getTypeFromStepDefinition() {
        for (Method method : this.getClass().getMethods()) {
            if (method.getName().equals("fake_step_definition")) {
                return method.getGenericParameterTypes()[0];
            }
        }
        throw new IllegalStateException();
    }

    @Test
    void unknown_target_type_does_no_transform_data_table() {
        StepDefinition stepDefinition = new StubStepDefinition("Given some stuff:", UNKNOWN_TYPE);
        StepExpression expression = stepExpressionFactory.createExpression(stepDefinition);
        List<Argument> match = expression.match("Given some stuff:", table);
        assertThat(match.get(0).getValue(), is(equalTo(DataTable.create(table))));
    }

    @Test
    void unknown_target_type_transform_doc_string_to_doc_string() {
        String docString = "A rather long and boring string of documentation";
        StepDefinition stepDefinition = new StubStepDefinition("Given some stuff:", UNKNOWN_TYPE);
        StepExpression expression = stepExpressionFactory.createExpression(stepDefinition);
        List<Argument> match = expression.match("Given some stuff:", docString, null);
        assertThat(match.get(0).getValue(), is(equalTo(DocString.create(docString))));
    }

    @Test
    void docstring_expression_transform_doc_string_with_content_type_to_string() {
        String docString = "A rather long and boring string of documentation";
        String contentType = "doc";
        StepDefinition stepDefinition = new StubStepDefinition("Given some stuff:", String.class);
        StepExpression expression = stepExpressionFactory.createExpression(stepDefinition);
        List<Argument> match = expression.match("Given some stuff:", docString, contentType);
        assertThat(match.get(0).getValue(), is(equalTo(docString)));
    }

    @Test
    void docstring_expression_transform_doc_string_to_json_node() {
        String docString = "{\"hello\": \"world\"}";
        String contentType = "json";
        registry.defineDocStringType(new DocStringType(
            JsonNode.class,
            contentType,
            (String s) -> objectMapper.convertValue(docString, JsonNode.class)));

        StepDefinition stepDefinition = new StubStepDefinition("Given some stuff:", JsonNode.class);
        StepExpression expression = stepExpressionFactory.createExpression(stepDefinition);
        List<Argument> match = expression.match("Given some stuff:", docString, contentType);
        JsonNode node = (JsonNode) match.get(0).getValue();
        assertThat(node.asText(), equalTo(docString));
    }

    @SuppressWarnings("unchecked")
    @Test
    void empty_table_cells_are_presented_as_null_to_transformer() {
        registry.setDefaultDataTableEntryTransformer(
            (map, valueType, tableCellByTypeTransformer) -> objectMapper.convertValue(map,
                objectMapper.constructType(valueType)));

        StepDefinition stepDefinition = new StubStepDefinition("Given some stuff:", getTypeFromStepDefinition());
        StepExpression expression = stepExpressionFactory.createExpression(stepDefinition);
        List<List<String>> table = asList(asList("name", "amount", "unit"), asList("chocolate", null, "tbsp"));
        List<Argument> match = expression.match("Given some stuff:", table);

        List<Ingredient> ingredients = (List<Ingredient>) match.get(0).getValue();
        Ingredient ingredient = ingredients.get(0);
        assertThat(ingredient.name, is(equalTo("chocolate")));

    }

    @SuppressWarnings("unused")
    public void fake_step_definition(List<Ingredient> ingredients) {

    }

    static class Ingredient {

        public String name;
        public Integer amount;
        public String unit;

        Ingredient() {
        }

    }

}
