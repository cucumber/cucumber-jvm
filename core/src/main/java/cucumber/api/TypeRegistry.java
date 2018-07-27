package cucumber.api;

import io.cucumber.cucumberexpressions.ParameterType;
import io.cucumber.datatable.DataTableType;
import io.cucumber.datatable.TableCellByTypeTransformer;
import io.cucumber.datatable.TableEntryByTypeTransformer;

public interface TypeRegistry {

    void defineParameterType(ParameterType<?> parameterType);

    void defineDataTableType(DataTableType tableType);

    /**
     * Set default transformer for entries which are not defined by
     * {@code defineDataTableType(new DataTableType(Class<T>,TableEntryTransformer<T>))}
     *
     * @param tableEntryByTypeTransformer default transformer
     */
    void setDefaultDataTableEntryTransformer(TableEntryByTypeTransformer tableEntryByTypeTransformer);

    /**
     * Set default transformer for cells which are not defined by
     * {@code defineDataTableType(new DataTableType(Class<T>,TableEntryTransformer<T>))}
     *
     * @param tableCellByTypeTransformer default transformer
     */
    void setDefaultDataTableCellTransformer(TableCellByTypeTransformer tableCellByTypeTransformer);
}
