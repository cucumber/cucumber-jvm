package io.cucumber.core.stepexpression;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.datatable.DataTable;
import io.cucumber.datatable.DataTableType;
import io.cucumber.datatable.TableEntryTransformer;
import io.cucumber.datatable.TableTransformer;
import io.cucumber.docstring.DocString;
import io.cucumber.docstring.DocStringType;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

class StepExpressionFactoryTest {

    private static final TypeResolver UNKNOWN_TYPE = () -> Object.class;
    private final ObjectMapper objectMapper = new ObjectMapper();

    static class Ingredient {
        public String name;
        public Integer amount;
        public String unit;

        Ingredient() {
        }
    }

    private final TypeRegistry registry = new TypeRegistry(Locale.ENGLISH);
    private final List<List<String>> table = asList(asList("name", "amount", "unit"), asList("chocolate", "2", "tbsp"));
    private final List<List<String>> tableTransposed = asList(asList("name", "chocolate"), asList("amount", "2"), asList("unit", "tbsp"));


    private TableEntryTransformer<Ingredient> listBeanMapper(final TypeRegistry registry) {
        //Just pretend this is a bean mapper.
        return tableRow -> {
            Ingredient bean = new Ingredient();
            bean.amount = Integer.valueOf(tableRow.get("amount"));
            bean.name = tableRow.get("name");
            bean.unit = tableRow.get("unit");
            return bean;
        };
    }


    private TableTransformer<Ingredient> beanMapper(final TypeRegistry registry) {
        return table -> {
            Map<String, String> tableRow = table.transpose().asMaps().get(0);
            return listBeanMapper(registry).transform(tableRow);
        };
    }


    @Test
    void table_expression_with_type_creates_table_from_table() {

        StepExpression expression = new StepExpressionFactory(registry).createExpression("Given some stuff:", DataTable.class);


        List<Argument> match = expression.match("Given some stuff:", table);

        DataTable dataTable = (DataTable) match.get(0).getValue();
        assertThat(dataTable.cells(), is(equalTo(table)));
    }

    @Test
    void table_expression_with_type_creates_single_ingredients_from_table() {

        registry.defineDataTableType(new DataTableType(Ingredient.class, beanMapper(registry)));
        StepExpression expression = new StepExpressionFactory(registry).createExpression("Given some stuff:", Ingredient.class);
        List<Argument> match = expression.match("Given some stuff:", tableTransposed);


        Ingredient ingredient = (Ingredient) match.get(0).getValue();
        assertThat(ingredient.name, is(equalTo("chocolate")));
    }

    @SuppressWarnings("unchecked")
    @Test
    void table_expression_with_list_type_creates_list_of_ingredients_from_table() {

        registry.defineDataTableType(new DataTableType(Ingredient.class, listBeanMapper(registry)));

        StepExpression expression = new StepExpressionFactory(registry).createExpression("Given some stuff:", getTypeFromStepDefinition());
        List<Argument> match = expression.match("Given some stuff:", table);

        List<Ingredient> ingredients = (List<Ingredient>) match.get(0).getValue();
        Ingredient ingredient = ingredients.get(0);
        assertThat(ingredient.amount, is(equalTo(2)));
    }

    @Test
    void unknown_target_type_does_no_transform_data_table() {
        StepExpression expression = new StepExpressionFactory(registry).createExpression("Given some stuff:", UNKNOWN_TYPE);
        List<Argument> match = expression.match("Given some stuff:", table);
        assertThat(match.get(0).getValue(), is(equalTo(DataTable.create(table))));
    }

    @Test
    void unknown_target_type_transform_doc_string_to_doc_string() {
        String docString = "A rather long and boring string of documentation";
        StepExpression expression = new StepExpressionFactory(registry).createExpression("Given some stuff:", UNKNOWN_TYPE);
        List<Argument> match = expression.match("Given some stuff:", docString, null);
        assertThat(match.get(0).getValue(), is(equalTo(DocString.create(docString))));
    }

    @Test
    void docstring_expression_transform_doc_string_with_content_type_to_string() {
        String docString = "A rather long and boring string of documentation";
        String contentType = "doc";
        StepExpression expression = new StepExpressionFactory(registry).createExpression("Given some stuff:", String.class);
        List<Argument> match = expression.match("Given some stuff:", docString, contentType);
        assertThat(match.get(0).getValue(), is(equalTo(docString)));
    }

    @Test
    void docstring_expression_transform_doc_string_to_json_node() {
        String docString = "{\"hello\": \"world\"}";
        String contentType = "json";
        registry.defineDocStringType(new DocStringType(JsonNode.class, contentType, (String s) -> objectMapper.convertValue(docString, JsonNode.class)));

        StepExpression expression = new StepExpressionFactory(registry).createExpression("Given some stuff:", JsonNode.class);
        List<Argument> match = expression.match("Given some stuff:", docString, contentType);
        JsonNode node = (JsonNode) match.get(0).getValue();
        assertThat(node.asText(), equalTo(docString));
    }

    @SuppressWarnings("unchecked")
    @Test
    void empty_table_cells_are_presented_as_null_to_transformer() {
        registry.setDefaultDataTableEntryTransformer(
            (map, valueType, tableCellByTypeTransformer) -> objectMapper.convertValue(map, objectMapper.constructType(valueType)));

        StepExpression expression = new StepExpressionFactory(registry).createExpression("Given some stuff:", getTypeFromStepDefinition());
        List<List<String>> table = asList(asList("name", "amount", "unit"), asList("chocolate", null, "tbsp"));
        List<Argument> match = expression.match("Given some stuff:", table);

        List<Ingredient> ingredients = (List<Ingredient>) match.get(0).getValue();
        Ingredient ingredient = ingredients.get(0);
        assertThat(ingredient.name, is(equalTo("chocolate")));

    }

    private Type getTypeFromStepDefinition() {
        for (Method method : this.getClass().getMethods()) {
            if (method.getName().equals("fake_step_definition")) {
                return method.getGenericParameterTypes()[0];
            }
        }
        throw new IllegalStateException();
    }


    @SuppressWarnings("unused")
    public void fake_step_definition(List<Ingredient> ingredients) {

    }

}
