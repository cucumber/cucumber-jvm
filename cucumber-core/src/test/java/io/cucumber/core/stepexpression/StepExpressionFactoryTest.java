package io.cucumber.core.stepexpression;

import io.cucumber.core.backend.StepDefinition;
import io.cucumber.core.backend.StubStepDefinition;
import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.exception.CucumberException;
import io.cucumber.core.gherkin.Step;
import io.cucumber.core.runtime.TimeServiceEventBus;
import io.cucumber.cucumberexpressions.CucumberExpression;
import io.cucumber.datatable.DataTable;
import io.cucumber.datatable.DataTableType;
import io.cucumber.datatable.TableEntryTransformer;
import io.cucumber.datatable.TableTransformer;
import io.cucumber.docstring.DocString;
import io.cucumber.docstring.DocStringType;
import io.cucumber.messages.types.Envelope;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.shadow.de.siegmar.fastcsv.util.Nullable;
import org.mockito.Mockito;
import tools.jackson.core.StreamWriteFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.cfg.ConstructorDetector;
import tools.jackson.databind.json.JsonMapper;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableWithSize.iterableWithSize;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@SuppressWarnings("NullAway") // TODO: Use AssertJ
public class StepExpressionFactoryTest {

    private static final Type UNKNOWN_TYPE = Object.class;
    private static final JsonMapper objectMapper = JsonMapper.builder()
            .changeDefaultPropertyInclusion(value -> value
                    .withContentInclusion(NON_ABSENT)
                    .withValueInclusion(NON_ABSENT))
            .constructorDetector(ConstructorDetector.USE_PROPERTIES_BASED)
            .disable(StreamWriteFeature.AUTO_CLOSE_TARGET)
            .build();
    private final EventBus bus = new TimeServiceEventBus(Clock.systemUTC(),
        UUID::randomUUID);
    private final StepTypeRegistry registry = new StepTypeRegistry(Locale.ENGLISH);
    private final StepExpressionFactory stepExpressionFactory = new StepExpressionFactory(registry, bus);

    private final String content = "A rather long and boring string of documentation";
    private final List<List<String>> table = asList(asList("name", "amount",
        "unit"), asList("chocolate", "2", "tbsp"));
    private final List<List<String>> tableTransposed = asList(asList("name",
        "chocolate"), asList("amount", "2"),
        asList("unit", "tbsp"));

    private static Step step(String text) {
        var step = Mockito.mock(Step.class);
        when(step.getText()).thenReturn(text);
        return step;
    }

    private Step stepWithTable(String text, List<List<String>> cells) {
        var step = step(text);
        var table = table(cells);
        when(step.getArguments()).thenReturn(List.of(table));
        return step;
    }

    private static io.cucumber.core.gherkin.DataTableArgument table(List<List<String>> cells) {
        var table = Mockito.mock(io.cucumber.core.gherkin.DataTableArgument.class);
        when(table.cells()).thenReturn(cells);
        return table;
    }

    private Step stepWithDocString(String text, String content, @Nullable String mediaType) {
        var step = step(text);
        var docString = docString(content, mediaType);
        when(step.getArguments()).thenReturn(List.of(docString));
        return step;
    }

    private static io.cucumber.core.gherkin.DocStringArgument docString(String content, @Nullable String mediaType) {
        var docString = Mockito.mock(io.cucumber.core.gherkin.DocStringArgument.class);
        when(docString.getContent()).thenReturn(content);
        when(docString.getMediaType()).thenReturn(mediaType);
        return docString;
    }

    private Step stepWithTableDocString(String text, List<List<String>> cells, String content) {
        var step = step(text);
        var table = table(cells);
        var docString = docString(content, null);
        when(step.getArguments()).thenReturn(List.of(table, docString));
        return step;
    }

    private Step stepWithDocStringTable(String text, String content, List<List<String>> cells) {
        var step = step(text);
        var table = table(cells);
        var docString = docString(content, null);
        when(step.getArguments()).thenReturn(List.of(docString, table));
        return step;
    }

    @Test
    void creates_a_step_expression() {
        StepDefinition stepDefinition = new StubStepDefinition("Given a stepWithTable");
        StepExpression expression = stepExpressionFactory.createExpression(stepDefinition);
        assertThat(expression.getSource(), is("Given a stepWithTable"));
        assertThat(expression.getExpressionType(), is(CucumberExpression.class));
        assertThat(expression.match(step("Given a stepWithTable")), is(emptyList()));
    }

    @Test
    void throws_for_unknown_parameter_types() {
        StepDefinition stepDefinition = new StubStepDefinition("Given a {unknownParameterType}");

        List<Envelope> events = new ArrayList<>();
        bus.registerHandlerFor(Envelope.class, events::add);

        CucumberException exception = assertThrows(
            CucumberException.class,
            () -> stepExpressionFactory.createExpression(stepDefinition));
        assertThat(exception.getMessage(), is("""
                Could not create a cucumber expression for 'Given a {unknownParameterType}'.
                It appears you did not register a parameter type."""

        ));
        assertThat(events, iterableWithSize(1));
        assertNotNull(events.get(0).getUndefinedParameterType());
    }

    @Test
    void table_expression_with_type_creates_table_from_table() {

        StepDefinition stepDefinition = new StubStepDefinition("Given some stuff:",
            DataTable.class);
        StepExpression expression = stepExpressionFactory.createExpression(stepDefinition);

        List<Argument> match = expression.match(stepWithTable("Given some stuff:", table));

        DataTable dataTable = (DataTable) match.get(0).getValue();
        assertThat(dataTable.cells(), is(equalTo(table)));
    }

    @Test
    void table_expression_with_type_creates_single_ingredients_from_table() {

        registry.defineDataTableType(new DataTableType(Ingredient.class,
            beanMapper()));
        StepDefinition stepDefinition = new StubStepDefinition("Given some stuff:",
            Ingredient.class);
        StepExpression expression = stepExpressionFactory.createExpression(stepDefinition);
        List<Argument> match = expression.match(stepWithTable("Given some stuff:",
            tableTransposed));

        Ingredient ingredient = (Ingredient) match.get(0).getValue();
        assertThat(ingredient.name, is(equalTo("chocolate")));
    }

    private TableTransformer<Ingredient> beanMapper() {
        return table -> {
            Map<String, String> tableRow = table.transpose().entries().get(0);
            return listBeanMapper().transform(tableRow);
        };
    }

    private TableEntryTransformer<Ingredient> listBeanMapper() {
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
        registry.defineDataTableType(new DataTableType(Ingredient.class,
            listBeanMapper()));

        StepDefinition stepDefinition = new StubStepDefinition("Given some stuff:",
            getTypeFromStepDefinition());
        StepExpression expression = stepExpressionFactory.createExpression(stepDefinition);
        List<Argument> match = expression.match(stepWithTable("Given some stuff:", table));

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
        StepDefinition stepDefinition = new StubStepDefinition("Given some stuff:",
            UNKNOWN_TYPE);
        StepExpression expression = stepExpressionFactory.createExpression(stepDefinition);
        List<Argument> match = expression.match(stepWithTable("Given some stuff:", table));
        assertThat(match.get(0).getValue(), is(equalTo(DataTable.create(table))));
    }

    @Test
    void unknown_target_type_transform_doc_string_to_doc_string() {
        StepDefinition stepDefinition = new StubStepDefinition("Given some stuff:",
            UNKNOWN_TYPE);
        StepExpression expression = stepExpressionFactory.createExpression(stepDefinition);
        List<Argument> match = expression.match(stepWithDocString("Given some stuff:", content,
            null));
        assertThat(match.get(0).getValue(),
            is(equalTo(DocString.create(content))));
    }

    @Test
    void docstring_expression_transform_doc_string_to_string() {
        StepDefinition stepDefinition = new StubStepDefinition("Given some stuff:",
            String.class);
        StepExpression expression = stepExpressionFactory.createExpression(stepDefinition);
        List<Argument> match = expression.match(stepWithDocString("Given some stuff:", content,
            null));
        assertThat(match.get(0).getValue(), is(equalTo(content)));
    }

    @Test
    void docstring_and_datatable_match_same_step_definition() {
        StepDefinition stepDefinition = new StubStepDefinition("Given some stuff:",
            UNKNOWN_TYPE);
        StepExpression expression = stepExpressionFactory.createExpression(stepDefinition);
        List<Argument> match = expression.match(stepWithDocString("Given some stuff:", content,
            null));
        assertThat(match.get(0).getValue(),
            is(equalTo(DocString.create(content))));
        match = expression.match(stepWithTable("Given some stuff:", table));
        assertThat(match.get(0).getValue(), is(equalTo(DataTable.create(table))));
    }

    @Test
    void docstring_expression_transform_doc_string_to_json_node() {
        String docString = "{\"hello\": \"world\"}";
        String contentType = "json";
        registry.defineDocStringType(new DocStringType(
            JsonNode.class,
            contentType,
            (String s) -> objectMapper.convertValue(docString, JsonNode.class)));

        StepDefinition stepDefinition = new StubStepDefinition("Given some stuff:",
            JsonNode.class);
        StepExpression expression = stepExpressionFactory.createExpression(stepDefinition);
        List<Argument> match = expression.match(stepWithDocString("Given some stuff:", docString,
            contentType));
        JsonNode node = (JsonNode) match.get(0).getValue();
        assertThat(node.asString(), equalTo(docString));
    }

    @SuppressWarnings("unchecked")
    @Test
    void empty_table_cells_are_presented_as_null_to_transformer() {
        registry.setDefaultDataTableEntryTransformer(
            (map, valueType, tableCellByTypeTransformer) -> objectMapper.convertValue(map,
                objectMapper.constructType(valueType)));

        StepDefinition stepDefinition = new StubStepDefinition("Given some stuff:",
            getTypeFromStepDefinition());
        StepExpression expression = stepExpressionFactory.createExpression(stepDefinition);
        List<List<String>> table = asList(asList("name", "amount", "unit"),
            asList("chocolate", "", "tbsp"));
        List<Argument> match = expression.match(stepWithTable("Given some stuff:", table));

        List<Ingredient> ingredients = (List<Ingredient>) match.get(0).getValue();
        Ingredient ingredient = ingredients.get(0);
        assertThat(ingredient.name, is(equalTo("chocolate")));

    }

    @Test
    void table_doc_string_expression() {
        StepDefinition stepDefinition = new StubStepDefinition("Given some stuff:", DataTable.class, DocString.class);
        StepExpression expression = stepExpressionFactory.createExpression(stepDefinition);
        List<Argument> match = expression.match(stepWithTableDocString("Given some stuff:", table, content));

        DataTable dataTable = (DataTable) match.get(0).getValue();
        assertThat(dataTable.cells(), is(equalTo(table)));
        DocString docString = (DocString) match.get(1).getValue();
        assertThat(docString.getContent(), is(equalTo(content)));
    }

    @Test
    void doc_string_table_expression() {
        StepDefinition stepDefinition = new StubStepDefinition("Given some stuff:", DocString.class, DataTable.class);
        StepExpression expression = stepExpressionFactory.createExpression(stepDefinition);
        List<Argument> match = expression.match(stepWithDocStringTable("Given some stuff:", content, table));

        DocString docString = (DocString) match.get(0).getValue();
        assertThat(docString.getContent(), is(equalTo(content)));
        DataTable dataTable = (DataTable) match.get(1).getValue();
        assertThat(dataTable.cells(), is(equalTo(table)));
    }

    @SuppressWarnings("unused")
    public void fake_step_definition(List<Ingredient> ingredients) {

    }

    public static final class Ingredient {

        private String name;
        private Integer amount;
        private String unit;

        public Ingredient() {
            /* no-op */
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getAmount() {
            return amount;
        }

        public void setAmount(Integer amount) {
            this.amount = amount;
        }

        public String getUnit() {
            return unit;
        }

        public void setUnit(String unit) {
            this.unit = unit;
        }
    }

}
