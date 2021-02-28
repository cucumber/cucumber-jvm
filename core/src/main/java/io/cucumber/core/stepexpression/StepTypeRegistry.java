package io.cucumber.core.stepexpression;

import io.cucumber.cucumberexpressions.ParameterByTypeTransformer;
import io.cucumber.cucumberexpressions.ParameterType;
import io.cucumber.cucumberexpressions.ParameterTypeRegistry;
import io.cucumber.datatable.DataTableType;
import io.cucumber.datatable.DataTableTypeRegistry;
import io.cucumber.datatable.TableCellByTypeTransformer;
import io.cucumber.datatable.TableEntryByTypeTransformer;
import io.cucumber.docstring.DocStringType;
import io.cucumber.docstring.DocStringTypeRegistry;

import java.util.Locale;

public final class StepTypeRegistry implements io.cucumber.core.api.TypeRegistry {

    private final ParameterTypeRegistry parameterTypeRegistry;

    private final DataTableTypeRegistry dataTableTypeRegistry;

    private final DocStringTypeRegistry docStringTypeRegistry;

    public StepTypeRegistry(Locale locale) {
        parameterTypeRegistry = new ParameterTypeRegistry(locale);
        dataTableTypeRegistry = new DataTableTypeRegistry(locale);
        docStringTypeRegistry = new DocStringTypeRegistry();
    }

    public ParameterTypeRegistry parameterTypeRegistry() {
        return parameterTypeRegistry;
    }

    public DataTableTypeRegistry dataTableTypeRegistry() {
        return dataTableTypeRegistry;
    }

    public DocStringTypeRegistry docStringTypeRegistry() {
        return docStringTypeRegistry;
    }

    @Override
    public void defineParameterType(ParameterType<?> parameterType) {
        parameterTypeRegistry.defineParameterType(parameterType);
    }

    @Override
    public void defineDocStringType(DocStringType docStringType) {
        docStringTypeRegistry.defineDocStringType(docStringType);
    }

    @Override
    public void defineDataTableType(DataTableType tableType) {
        dataTableTypeRegistry.defineDataTableType(tableType);
    }

    @Override
    public void setDefaultParameterTransformer(ParameterByTypeTransformer defaultParameterByTypeTransformer) {
        parameterTypeRegistry.setDefaultParameterTransformer(defaultParameterByTypeTransformer);
    }

    @Override
    public void setDefaultDataTableEntryTransformer(
            TableEntryByTypeTransformer defaultDataTableEntryByTypeTransformer
    ) {
        dataTableTypeRegistry.setDefaultDataTableEntryTransformer(defaultDataTableEntryByTypeTransformer);
    }

    @Override
    public void setDefaultDataTableCellTransformer(TableCellByTypeTransformer defaultDataTableByTypeTransformer) {
        dataTableTypeRegistry.setDefaultDataTableCellTransformer(defaultDataTableByTypeTransformer);
    }

}
