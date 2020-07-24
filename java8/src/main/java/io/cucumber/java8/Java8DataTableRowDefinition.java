package io.cucumber.java8;

import io.cucumber.core.backend.DataTableTypeDefinition;
import io.cucumber.datatable.DataTableType;

import java.util.List;

final class Java8DataTableRowDefinition extends AbstractDatatableElementTransformerDefinition
        implements DataTableTypeDefinition {

    private final DataTableType dataTableType;

    Java8DataTableRowDefinition(String[] emptyPatterns, DataTableRowDefinitionBody<?> body) {
        super(body, new Exception().getStackTrace()[3], emptyPatterns);
        Class<?> returnType = resolveRawArguments(DataTableRowDefinitionBody.class, body.getClass())[0];
        this.dataTableType = new DataTableType(
            returnType,
            (List<String> row) -> invokeMethod(replaceEmptyPatternsWithEmptyString(row)));
    }

    @Override
    public DataTableType dataTableType() {
        return dataTableType;
    }

}
