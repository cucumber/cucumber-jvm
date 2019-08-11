package io.cucumber.core.stepexpression;

import io.cucumber.cucumberexpressions.Expression;
import io.cucumber.cucumberexpressions.ExpressionFactory;
import io.cucumber.cucumberexpressions.ParameterByTypeTransformer;
import io.cucumber.cucumberexpressions.ParameterType;
import io.cucumber.datatable.DataTable;
import io.cucumber.datatable.DataTableType;
import io.cucumber.datatable.TableCellByTypeTransformer;
import io.cucumber.datatable.TableEntryByTypeTransformer;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.Map;
import java.util.regex.Pattern;

import static java.util.Locale.ENGLISH;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class TypeRegistryTest {

    private final TypeRegistry registry = new TypeRegistry(ENGLISH);
    private final ExpressionFactory expressionFactory = new ExpressionFactory(registry.parameterTypeRegistry());

    @Test
    public void should_define_parameter_type() {
        ParameterType<Object> expected = new ParameterType<>(
            "example",
            ".*",
            Object.class,
            (String s) -> null
        );
        registry.defineParameterType(expected);
        Expression expresion = expressionFactory.createExpression("{example}");
        assertThat(expresion.getRegexp().pattern(), is("^(.*)$"));
    }

    @Test
    public void should_define_data_table_parameter_type() {
        DataTableType expected = new DataTableType(Date.class, (DataTable dataTable) -> null);
        registry.defineDataTableType(expected);
    }

    @Test
    public void should_set_default_parameter_transformer() {
        ParameterByTypeTransformer expected = (fromValue, toValueType) -> null;
        registry.setDefaultParameterTransformer(expected);
    }

    @Test
    public void should_set_default_table_cell_transformer() {
        TableCellByTypeTransformer expected = new TableCellByTypeTransformer() {
            @Override
            public <T> T transform(String s, Class<T> aClass) {
                return null;
            }
        };
        registry.setDefaultDataTableCellTransformer(expected);
    }

    @Test
    public void should_set_default_table_entry_transformer() {
        TableEntryByTypeTransformer expected = new TableEntryByTypeTransformer() {
            @Override
            public <T> T transform(Map<String, String> map, Class<T> aClass, TableCellByTypeTransformer tableCellByTypeTransformer) {
                return null;
            }
        };
        registry.setDefaultDataTableEntryTransformer(expected);
    }

}
