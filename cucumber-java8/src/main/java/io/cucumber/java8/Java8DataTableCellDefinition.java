package io.cucumber.java8;

import io.cucumber.datatable.DataTableType;
import org.jspecify.annotations.Nullable;

final class Java8DataTableCellDefinition extends Java8DataTableTypeDefinition {

    private final DataTableType dataTableType;

    Java8DataTableCellDefinition(String[] emptyPatterns, DataTableCellDefinitionBody<?> body) {
        super(body, new Exception().getStackTrace()[3], emptyPatterns);
        Class<?> returnType = resolveRawArguments(DataTableCellDefinitionBody.class, body.getClass())[0];
        this.dataTableType = new DataTableType(
            returnType,
            (@Nullable String cell) -> invokeMethod(replaceEmptyPatternsWithEmptyString(cell)));
    }

    @Override
    public DataTableType dataTableType() {
        return dataTableType;
    }

}
