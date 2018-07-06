package cucumber.api;

import io.cucumber.cucumberexpressions.ParameterType;
import io.cucumber.datatable.DataTableType;
import io.cucumber.datatable.DefaultDataTableEntryTransformer;
import io.cucumber.datatable.TableCellByTypeTransformer;

public interface TypeRegistry {

    void defineParameterType(ParameterType<?> parameterType);

    void defineDataTableType(DataTableType tableType);

    /**
     * Set default transformer for entries which are not defined by {@code defineDataTableType(new DataTableType(Class<T>,TableEntryTransformer<T>))}
     */
    void setDefaultDataTableEntryTransformer(DefaultDataTableEntryTransformer defaultDataTableEntryTransformer);

    /**
     * Set default transformer for cells which are not defined by {@code defineDataTableType(new DataTableType(Class<T>,TableCellTransformer<T>))}
     */
    void setDefaultDataTableCellTransformer(TableCellByTypeTransformer defaultDataTableCellTransformer);
}
