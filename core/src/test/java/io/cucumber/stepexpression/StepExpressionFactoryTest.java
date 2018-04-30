package io.cucumber.stepexpression;

import io.cucumber.datatable.DataTable;
import io.cucumber.datatable.DataTableType;
import io.cucumber.datatable.TableEntryTransformer;
import io.cucumber.datatable.TableTransformer;
import org.junit.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class StepExpressionFactoryTest {
    static class Ingredient {
        String name;
        Integer amount;
        String unit;

        Ingredient() {
        }
    }

    private final TypeRegistry registry = new TypeRegistry(Locale.ENGLISH);
    private final List<List<String>> table = asList(asList("name", "amount", "unit"), asList("chocolate", "2", "tbsp"));
    private final List<List<String>> tableTransposed = asList(asList("name", "chocolate"), asList("amount", "2"), asList("unit", "tbsp"));


    private TableEntryTransformer<Ingredient> listBeanMapper(final TypeRegistry registry) {
        //Just pretend this is a bean mapper.
        return new TableEntryTransformer<Ingredient>() {

            @Override
            public Ingredient transform(Map<String, String> tableRow) {
                Ingredient bean = new Ingredient();
                bean.amount = Integer.valueOf(tableRow.get("amount"));
                bean.name = tableRow.get("name");
                bean.unit = tableRow.get("unit");
                return bean;
            }
        };
    }


    private TableTransformer<Ingredient> beanMapper(final TypeRegistry registry) {
        return new TableTransformer<Ingredient>() {
            @Override
            public Ingredient transform(DataTable table) throws Throwable {
                Map<String, String> tableRow = table.transpose().asMaps().get(0);
                return listBeanMapper(registry).transform(tableRow);
            }
        };
    }


    @Test
    public void table_expression_with_type_creates_table_from_table() {

        StepExpression expression = new StepExpressionFactory(registry).createExpression("Given some stuff:", DataTable.class);


        List<Argument> match = expression.match("Given some stuff:", table);

        DataTable dataTable = (DataTable) match.get(0).getValue();
        assertEquals(table, dataTable.cells());
    }

    @Test
    public void table_expression_with_type_creates_single_ingredients_from_table() {

        registry.defineDataTableType(new DataTableType(Ingredient.class, beanMapper(registry)));
        StepExpression expression = new StepExpressionFactory(registry).createExpression("Given some stuff:", Ingredient.class);
        List<Argument> match = expression.match("Given some stuff:", tableTransposed);


        Ingredient ingredient = (Ingredient) match.get(0).getValue();
        assertEquals(ingredient.name, "chocolate");
    }

    @Test
    public void table_expression_with_list_type_creates_list_of_ingredients_from_table() {

        registry.defineDataTableType(new DataTableType(Ingredient.class, listBeanMapper(registry)));

        StepExpression expression = new StepExpressionFactory(registry).createExpression("Given some stuff:", getTypeFromStepDefinition());
        List<Argument> match = expression.match("Given some stuff:", table);

        List<Ingredient> ingredients = (List<Ingredient>) match.get(0).getValue();
        Ingredient ingredient = ingredients.get(0);
        assertEquals(ingredient.name, "chocolate");
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
