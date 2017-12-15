package cucumber.runtime;

import cucumber.api.TypeRegistry;
import io.cucumber.cucumberexpressions.Argument;
import io.cucumber.cucumberexpressions.ParameterType;
import cucumber.api.datatable.DataTableType;
import cucumber.api.datatable.TableRowTransformer;
import cucumber.api.datatable.TableTransformer;
import cucumber.api.datatable.DataTable;
import org.junit.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
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


    private TableRowTransformer<Ingredient> listBeanMapper(final TypeRegistry registry) {
        //Just pretend this is a bean mapper.
        return new TableRowTransformer<Ingredient>() {

            @Override
            public Ingredient transform(Map<String, String> tableRow) {
                Ingredient bean = new Ingredient();
                ParameterType<Integer> intType = registry.lookupParameterTypeByType(Integer.class);
                bean.amount = intType.transform(singletonList(tableRow.get("amount")));
                ParameterType<String> stringType = registry.lookupParameterTypeByType(String.class);
                bean.name = stringType.transform(singletonList(tableRow.get("name")));
                bean.unit = stringType.transform(singletonList(tableRow.get("unit")));
                return bean;
            }
        };
    }


    private TableTransformer<Ingredient> beanMapper(final TypeRegistry registry) {
        return new TableTransformer<Ingredient>() {
            @Override
            public Ingredient transform(DataTable table) {
                Map<String, String> tableRow = table.transpose().asMaps().get(0);
                return listBeanMapper(registry).transform(tableRow);
            }
        };
    }


    @Test
    public void table_expression_with_type_creates_table_from_table() {

        StepExpression expression = new StepExpressionFactory(registry).createExpression("Given some stuff:", DataTable.class);


        List<Argument<?>> match = expression.match("Given some stuff:", table);

        DataTable dataTable = (DataTable) match.get(0).getValue();
        assertEquals(table, dataTable.cells());
    }

    @Test
    public void table_expression_with_name_creates_single_ingredients_from_table() {
        registry.defineDataTableType(new DataTableType("ingredient", Ingredient.class, beanMapper(registry)));

        StepExpression expression = new StepExpressionFactory(registry).createExpression("Given some stuff:", "ingredient");

        List<Argument<?>> match = expression.match("Given some stuff:", tableTransposed);
        Ingredient ingredient = (Ingredient) match.get(0).getValue();
        assertEquals(ingredient.name, "chocolate");
    }


    @Test
    public void table_expression_with_type_creates_single_ingredients_from_table() {

        registry.defineDataTableType(new DataTableType("ingredient", Ingredient.class, beanMapper(registry)));
        StepExpression expression = new StepExpressionFactory(registry).createExpression("Given some stuff:", Ingredient.class);
        List<Argument<?>> match = expression.match("Given some stuff:", tableTransposed);


        Ingredient ingredient = (Ingredient) match.get(0).getValue();
        assertEquals(ingredient.name, "chocolate");
    }


    @Test
    public void table_expression_with_name_creates_list_of_ingredients_from_table() {

        registry.defineDataTableType(new DataTableType("ingredients", Ingredient.class, listBeanMapper(registry)));
        StepExpression expression = new StepExpressionFactory(registry).createExpression("Given some stuff:", "ingredients");

        List<Argument<?>> match = expression.match("Given some stuff:", table);


        List<Ingredient> ingredients = (List<Ingredient>) match.get(0).getValue();
        Ingredient ingredient = ingredients.get(0);
        assertEquals(ingredient.name, "chocolate");
    }

    @Test
    public void table_expression_with_list_type_creates_list_of_ingredients_from_table() {

        registry.defineDataTableType(new DataTableType("ingredients", Ingredient.class, listBeanMapper(registry)));

        StepExpression expression = new StepExpressionFactory(registry).createExpression("Given some stuff:", getTypeFromStepDefinition());
        List<Argument<?>> match = expression.match("Given some stuff:", table);

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
