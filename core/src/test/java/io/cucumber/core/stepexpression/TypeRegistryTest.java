package io.cucumber.core.stepexpression;

import io.cucumber.cucumberexpressions.ParameterByTypeTransformer;
import io.cucumber.cucumberexpressions.ParameterType;
import io.cucumber.cucumberexpressions.Transformer;
import io.cucumber.datatable.DataTable;
import io.cucumber.datatable.DataTableType;
import io.cucumber.datatable.TableCellByTypeTransformer;
import io.cucumber.datatable.TableEntryByTypeTransformer;
import io.cucumber.datatable.TableTransformer;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.Map;

import static java.util.Locale.ENGLISH;
import static org.junit.Assert.assertEquals;


public class TypeRegistryTest {

    private final TypeRegistry registry = new TypeRegistry(ENGLISH);

    @Test
    public void should_define_parameter_type() {
        ParameterType<Object> expected = new ParameterType<>(
            "example",
            ".*",
            Object.class,
            new Transformer<Object>() {
                @Override
                public Object transform(String s) {
                    return null;
                }
            }
        );
        registry.defineParameterType(expected);
        assertEquals(expected, registry.parameterTypeRegistry().lookupByTypeName("example"));
    }

    @Test
    public void should_define_data_table_parameter_type() {
        DataTableType expected = new DataTableType(Date.class, new TableTransformer<Date>() {
            @Override
            public Date transform(DataTable dataTable) {
                return null;
            }
        });
        registry.defineDataTableType(expected);
        assertEquals(expected, registry.dataTableTypeRegistry().lookupTableTypeByType(Date.class));
    }

    @Test
    public void should_set_default_parameter_transformer() {
        ParameterByTypeTransformer expected = new ParameterByTypeTransformer() {
            @Override
            public Object transform(String fromValue, Type toValueType) {
                return null;
            }
        };
        registry.setDefaultParameterTransformer(expected);
        assertEquals(expected, registry.parameterTypeRegistry().getDefaultParameterTransformer());
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
