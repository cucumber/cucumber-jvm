package io.cucumber.core.api;

import io.cucumber.cucumberexpressions.ParameterByTypeTransformer;
import io.cucumber.cucumberexpressions.ParameterType;
import io.cucumber.datatable.DataTableType;
import io.cucumber.datatable.TableCellByTypeTransformer;
import io.cucumber.datatable.TableEntryByTypeTransformer;
import io.cucumber.docstring.DocStringType;
import org.apiguardian.api.API;

/**
 * The type registry records defines parameter types, data table types and docstring transformers.
 */
@API(status = API.Status.STABLE)
public interface TypeRegistry {
    /**
     * Defines a new parameter type.
     * @param parameterType The new parameter type.
     */
    void defineParameterType(ParameterType<?> parameterType);

    /**
     * Defines a new docstring type.
     * @param docStringType The new docstring type.
     */
    void defineDocStringType(DocStringType docStringType);

    /**
     * Defines a new data table type.
     * @param tableType The new table type.
     */
    void defineDataTableType(DataTableType tableType);

    /**
     * Set default transformer for parameters which are not defined by
     * {@code defineParameterType(ParameterType<?>))}
     *
     * @param defaultParameterByTypeTransformer default transformer
     */
    void setDefaultParameterTransformer(ParameterByTypeTransformer defaultParameterByTypeTransformer);
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
